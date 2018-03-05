package com.practicingtechie.dl

object NN2nd4j {
  import org.nd4j.linalg.api.ndarray.INDArray
  import org.nd4j.linalg.factory.Nd4j
  import org.nd4j.linalg.api.buffer.DataBuffer
  import org.nd4j.linalg.ops.transforms.Transforms.{sigmoid, relu, log, exp}
  import org.nd4j.linalg.indexing.conditions._

  //Nd4j.getRandom().setSeed(1)

  import com.practicingtechie.dl.Activation._

  val logger = com.typesafe.scalalogging.Logger(this.getClass)

  case class LCache(a: INDArray, w: INDArray, b: INDArray) {
    def tupled = (a, w, b)
  }
  case class ACache(z: INDArray)
  case class Cache(lc: LCache, ac: ACache)

  val initializeParameters = initializeParametersRandom _

  def initializeParametersRandom(nx: Int, nh: Int, ny: Int) = {
    val (w1, b1, w2, b2) = (
      Nd4j.randn(nh, nx).muli(0.01),
      Nd4j.zeros(nh, 1),
      Nd4j.randn(ny, nh).muli(0.01),
      Nd4j.zeros(ny, 1))
    (w1, b1, w2, b2)
  }

  def initializeParametersFromFile(nx: Int, nh: Int, ny: Int) = {
    def fromTsv(fn: String) = {
      import com.github.tototoshi.csv._
      val format = new TSVFormat {}
      val r = CSVReader.open(s"dev_data/2-layer/$fn")(format).all
      Nd4j.create(r.flatMap(e => e).map(_.toDouble).toArray, Array(r.length, r.head.length), 'c')
    }
    val (w1, b1, w2, b2) = (fromTsv("w1.tsv"), fromTsv("b1.tsv"), fromTsv("w2.tsv"), fromTsv("b2.tsv"))
    println(w1.getRow(0).getColumns(0, 1, 2, 3, 4, 5, 6, 7))

    (w1, b1, w2, b2)
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

  def recalculateParameters(parsWithGrads: List[(INDArray, INDArray)], alpha: Double) =
    parsWithGrads map {
      case (param, gradient) =>
        param.sub(gradient.mul(alpha))
    }

  def twoLayerModel(x: INDArray, y: INDArray, layersDims: (Int, Int, Int),
                    learningRate: Double = 0.0075, numIterations: Int = 3000, printCost: Boolean = false) = {

    val (nx, nh, ny) = layersDims

    0.until(numIterations).foldLeft(initializeParameters(nx, nh, ny)) {
      case (((w1, b1, w2, b2), i)) =>
        val (a1, cache1) = linearActivationForward(x, w1, b1, ReLu)
        val (a2, cache2) = linearActivationForward(a1, w2, b2, Sigmoid)
        val cost = computeCost(a2, y)
        if (printCost && i % 100 == 0)
          println(s"Cost after iteration $i: $cost")

        // dA2 = - (np.divide(Y, A2) - np.divide(1 - Y, 1 - A2))
        val dA2 = y.div(a2).subi(y.rsub(1).divi(a2.rsub(1))).negi()
        val (dA1, dW2, db2) = linearActivationBackward(dA2, cache2, Sigmoid)
        val (dA0, dW1, db1) = linearActivationBackward(dA1, cache1, ReLu)

        recalculateParameters(List((w1, dW1), (w2, dW2), (b1, db1), (b2, db2)), learningRate) match {
          case nw1 :: nw2 :: nb1 :: nb2 :: _ => (nw1, nb1, nw2, nb2)
          case x => throw new IllegalArgumentException(s"unexpected result: $x")
        }
    }

  }

  def predict(x: INDArray, y: INDArray, w1: INDArray, b1: INDArray, w2: INDArray, b2: INDArray): Double = {
    val (a1, _) = linearActivationForward(x, w1, b1, ReLu)
    val (a2, _) = linearActivationForward(a1, w2, b2, Sigmoid)
    val predictions = a2.cond(new GreaterThan(0.5))
    val accuracy = y.sub(predictions).cond(new EqualsCondition(0.0)).sumNumber().doubleValue / y.columns

    accuracy
  }

  def readData(fileName: String, xName: String, yName: String) = {
    val cdf = ucar.nc2.NetcdfFile.open(fileName)
    val (shapeX, xArr) = readInputData(cdf, xName)
    val inputLen = shapeX.drop(1).reduce(_ * _)
    val x = Nd4j.create(xArr, Array[Int](inputLen, shapeX.head.toInt), 'f')
    x.divi(255.0)
    println(x.shape().toList)

    val yArr = readLabels(cdf, yName)
    val y = Nd4j.create(yArr, Array(1, yArr.length), 'c')
    println(y.shape().toList)
    cdf.close

    (x, y)
  }

  def main(args: Array[String]): Unit = {
    val (hiddenNodes, outputNodes) = (7, 1)
    val learningRate = 0.0075
    val numIterations = 2500

    Nd4j.setDataType(DataBuffer.Type.DOUBLE)

    val (trainX, trainY) = readData(TrainSetFileName, "train_set_x", "train_set_y")
    val inputLen = trainX.shape()(0)

    val (w1, b1, w2, b2) = twoLayerModel(trainX, trainY, (inputLen, hiddenNodes, outputNodes), learningRate, numIterations, true)
    val accuracyTrain = predict(trainX, trainY, w1, b1, w2, b2)

    val (testX, testY) = readData(TestSetFileName, "test_set_x", "test_set_y")
    val accuracyTest = predict(testX, testY, w1, b1, w2, b2)

    println(s"train accuracy: $accuracyTrain")
    println(s"test accuracy: $accuracyTest")
  }

}
