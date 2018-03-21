package com.practicingtechie.dl

object Nd4jUtil {
  import org.nd4j.linalg.api.ndarray.INDArray
  import org.nd4j.linalg.factory.Nd4j
  import org.nd4j.linalg.ops.transforms.Transforms.{exp, log, relu, sigmoid}

  import com.practicingtechie.dl.Activation._

  case class LCache(a: INDArray, w: INDArray, b: INDArray) {
    def tupled = (a, w, b)
  }
  case class ACache(z: INDArray)
  case class Cache(lc: LCache, ac: ACache)

  def dims(m: INDArray) = m.rows -> m.columns

  case class Parameters(weights: List[INDArray], biases: List[INDArray]) {
    def layers: Int = weights.size
    def weightsAndBiases = weights zip biases
    def summary = {
      0.until(layers).toList.map { l =>
        s"l: $l: w: ${dims(weights(l))}; b: ${dims(biases(l))}"
      }.mkString(" ## ")
    }
  }

  case class LGradients(da: INDArray, dw: INDArray, db: INDArray)

  def matrixFromTsv(baseDir: String)(fn: String) = {
    import com.github.tototoshi.csv._
    val format = new TSVFormat {}
    val r = CSVReader.open(s"$baseDir/$fn")(format).all
    Nd4j.create(r.flatMap(e => e).map(_.toDouble).toArray, Array(r.length, r.head.length), 'c')
  }

  def linearForward(a: INDArray, w: INDArray, b: INDArray) =
    w.mmul(a).addiColumnVector(b) -> LCache(a, w, b)

  def linearActivationForward(aPrev: INDArray, w: INDArray, b: INDArray, activation: Activation) = {
    val (z, lCache) = linearForward(aPrev, w, b)
    val a = activation match {
      case Sigmoid => sigmoid(z)
      case ReLu => relu(z)
    }
    (a, Cache(lCache, ACache(z)))
  }

  def computeCost(aL: INDArray, y: INDArray): Double = {
    val m = y.shape()(1)

    val alT = aL.transpose()
    val v = y.mmul(log(alT)).addi(y.rsub(1).mmul(log(alT.rsub(1))))
    (-1.0/m) * v.getDouble(0)
  }

  def linearBackward(dZ: INDArray, lc: LCache) = {
    val (aPrev, w, b) = lc.tupled
    val m = aPrev.shape()(1)
    val dW = dZ.mmul(aPrev.transpose()).muli(1.0/m)
    val db = dZ.sum(1).muli(1.0/m)
    val dAPrev = w.transpose().mmul(dZ)
    (dAPrev, dW, db)
  }

  def reluBackward(dA: INDArray, z: INDArray) =
    dA.mul(z.gt(0))

  def sigmoidBackward(dA: INDArray, z: INDArray) = {
    val s = exp(z.neg()).addi(1).rdivi(1)
    dA.mulRowVector(s).muliRowVector(s.rsub(1))
  }

  def linearActivationBackward(dA: INDArray, cache: Cache, activation: Activation) = {
    val dZ = activation match {
      case Sigmoid => sigmoidBackward(dA, cache.ac.z)
      case ReLu => reluBackward(dA, cache.ac.z)
    }
    val (daPrev, dw, db) = linearBackward(dZ, cache.lc)
    (daPrev, dw, db)
  }

  def readData(fileName: String, xName: String, yName: String) = {
    val cdf = ucar.nc2.NetcdfFile.open(fileName)
    val (shapeX, xArr) = getDataSectionInput(cdf, xName)
    val inputLen = shapeX.drop(1).reduce(_ * _)
    val x = Nd4j.create(xArr, Array[Int](inputLen, shapeX.head.toInt), 'f')
    x.divi(255.0)
    println(x.shape().toList)

    val yArr = getDataSetSectionLabels(cdf, yName)
    val y = Nd4j.create(yArr, Array(1, yArr.length), 'c')
    println(y.shape().toList)
    cdf.close

    (x, y)
  }

}
