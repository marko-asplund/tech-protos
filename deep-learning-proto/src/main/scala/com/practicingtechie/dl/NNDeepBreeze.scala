package com.practicingtechie.dl

object NNDeepBreeze {
  import breeze.linalg._
  import breeze.numerics._
  import breeze.stats.distributions._

  import com.practicingtechie.dl.Activation._

  val logger = com.typesafe.scalalogging.Logger(this.getClass)

  case class LCache(a: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseMatrix[Double])
  case class ACache(z: DenseMatrix[Double])
  case class Cache(lc: LCache, ac: ACache)

  def dims(m: DenseMatrix[_]) = m.rows -> m.cols

  case class Parameters(weights: List[DenseMatrix[Double]], biases: List[DenseMatrix[Double]]) {
    def layers: Int = weights.size
    def weightsAndBiases = weights zip biases
    def summary = {
      0.until(layers).toList.map { l =>
        s"l: $l: w: ${dims(weights(l))}; b: ${dims(biases(l))}"
      }.mkString(" ## ")
    }
  }

  case class LGradients(da: DenseMatrix[Double], dw: DenseMatrix[Double], db: DenseMatrix[Double])

  //val initializeParametersDeep = initializeParametersDeepFromFile _
  val initializeParametersDeep = initializeParametersDeepRandom _

  def initializeParametersDeepRandom(layerDims: List[Int]): Parameters =
    Parameters.tupled(1.until(layerDims.size).toList.map { l =>
      DenseMatrix.rand[Double](layerDims(l), layerDims(l-1), Rand.gaussian(0, 1)) / sqrt(layerDims(l-1)) ->
        DenseMatrix.zeros[Double](layerDims(l), 1)
    }.unzip)


  def dumpParameters(p: Parameters): Unit = {
    def dumpMatrices(fileBase: String, matrices: List[DenseMatrix[Double]]) = 1.to(matrices.size).foreach { i =>
      csvwrite(new java.io.File(s"$fileBase$i.tsv"), matrices(i-1), '\t')
    }
    dumpMatrices("w", p.weights)
    dumpMatrices("b", p.biases)
  }

  def initializeParametersDeepFromFile(layerDims: List[Int]): Parameters = {
    import java.io.File
    def fromTsv(fn: String) = csvread(new File(new File("dev_data/l-layer"), fn), '\t')

    Parameters.tupled(1.to(4).toList.map { i =>
      fromTsv(s"w$i.tsv") -> fromTsv(s"b$i.tsv")
    }.unzip)
  }

  def initializeParameters(nx: Int, nh: Int, ny: Int): (DenseMatrix[Double], DenseMatrix[Double], DenseMatrix[Double], DenseMatrix[Double]) = ???

  def linearForward(a: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseMatrix[Double]) = {
    ((w * a).apply(::, *) + b(::, 0), LCache(a, w, b))
  }

  def linearActivationForward(aPrev: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseMatrix[Double], activation: Activation) = {
    val (z, lCache) = linearForward(aPrev, w, b)
    val a = activation match {
      case Sigmoid => sigmoid(z)
      case ReLu => z.map(e => Math.max(0, e))
    }
    (a, Cache(lCache, ACache(z)))
  }

  def computeCost(aL: DenseMatrix[Double], y: DenseMatrix[Double]): Double = {
    val c = log(aL(0, ::)) * y + log(aL.map(e => 1.0 - e).apply(0, ::)) * y.map(e => 1.0 - e)
    val r = (-1.0 / y.rows) * c
    sum(r)
  }

  def linearBackward(dZ: DenseMatrix[Double], aPrev: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseMatrix[Double]) = {
    val m: Int = aPrev.cols
    val dW = (1.0 / m) * (dZ * aPrev.t)
    val db = (1.0 / m) * sum(dZ, Axis._1)
    val dAPrev = w.t * dZ

    (dAPrev, dW, db)
  }

  def reluBackward(dA: DenseMatrix[Double], z: DenseMatrix[Double]) = {
    dA *:* z.map(e => if (e > 0) 1.0 else 0.0)
  }

  def sigmoidBackward(dA: DenseMatrix[Double], z: DenseMatrix[Double]) = {
    val s = sigmoid(z)
    dA *:* s *:* s.map(e => 1.0 - e)
  }

  def linearActivationBackward(dA: DenseMatrix[Double], cache: Cache, activation: Activation) = {
    val dZ = activation match {
      case Sigmoid => sigmoidBackward(dA, cache.ac.z)
      case ReLu => reluBackward(dA, cache.ac.z)
    }
    val (daPrev, dw, db) = linearBackward(dZ, cache.lc.a, cache.lc.w, cache.lc.b)
    (daPrev, dw, db)
  }

  def lModelForward(x: DenseMatrix[Double], p: Parameters) = {
    val (aPrev, caches) = 0.until(p.layers - 1).toList.foldLeft((x, List.empty[Cache])) {
      case ((aPrev, caches), l) =>
        val (a, cache) = linearActivationForward(aPrev, p.weights(l), p.biases(l), ReLu)

        a -> (caches :+ cache)
    }
    val (al, cache) = linearActivationForward(aPrev, p.weights.last, p.biases.last, Sigmoid)

    al -> (caches :+ cache)
  }

  def lModelBackward(al: DenseMatrix[Double], yOrig: DenseMatrix[Double], caches: List[Cache]) = {
    val layers = caches.size
    val y = yOrig.toDenseMatrix.reshape(al.rows, al.cols)
    // dAL = - (np.divide(Y, AL) - np.divide(1 - Y, 1 - AL))
    val dAl = -((y / al) -  (y.map(e => 1.0 - e) / al.map(e => 1.0 - e)))
    val (lda, ldw, ldb) = linearActivationBackward(dAl, caches.last, Sigmoid)
    val gradients = 0.until(layers-1).toList.reverse.foldLeft(List(LGradients(lda, ldw, ldb.toDenseMatrix.t))) {
      case (grads, l) =>
        val (da, dw, db) = linearActivationBackward(grads.last.da, caches(l), ReLu)
        grads :+ LGradients(da, dw, db.toDenseMatrix.t)
    }

    gradients.reverse
  }

  def recalculateParametersDeep(parameters: Parameters, gradients: List[LGradients], learningRate: Double) = {
    Parameters.tupled(parameters.weightsAndBiases.zip(gradients).map {
      case ((weight, bias), grad) =>
        (weight - (grad.dw * learningRate)) ->
          (bias - (grad.db * learningRate))
    }.unzip)
  }

  def lLayerModel(x: DenseMatrix[Double], y: DenseMatrix[Double], layersDims: List[Int],
                  learningRate: Double = 0.0075, numIterations: Int = 3000, printCost: Boolean = false) = {

    0.until(numIterations).foldLeft(initializeParametersDeep(layersDims)) {
      case (parameters, i) =>
        val (al, caches) = lModelForward(x, parameters)
        val cost = computeCost(al, y)
        if (printCost && i % 100 == 0)
          println(s"Cost after iteration $i: $cost")
        val gradients = lModelBackward(al, y, caches)

        recalculateParametersDeep(parameters, gradients, learningRate)
    }
  }

  def predictDeep(x: DenseMatrix[Double], y: DenseMatrix[Double], parameters: Parameters): Double = {
    val (al, _) = lModelForward(x, parameters)
    val predictions = al.map(p => if (p > 0.5) 1.0 else 0.0)
    val accuracy = sum((y.toDenseMatrix :== predictions).map(v => if(v) 1.0 else 0.0)) / y.cols.toDouble

    accuracy
  }

  def readData(fileName: String, xName: String, yName: String) = {
    val cdf = ucar.nc2.NetcdfFile.open(fileName)
    val (shapeX, xArr) = readInputData(cdf, xName)
    val inputLen = shapeX.drop(1).reduce(_ * _)
    0.until(xArr.size).foreach(i => xArr.update(i, xArr(i) / 255.0))
    val x = new DenseMatrix(inputLen, shapeX.head, xArr)
    println(s"X dims: ${dim(x)}")

    val yArr = readLabels(cdf, yName)
    val y = new DenseMatrix(yArr.length, 1, yArr)
    println(s"y dims: ${dim(y)}")
    cdf.close

    (x, y)
  }

  def main(args: Array[String]): Unit = {
    val layersDims = List(12288, 20, 7, 5, 1)
    val learningRate = 0.0075
    val numIterations = 2500

    val (trainX, trainY) = readData(TrainSetFileName, "train_set_x", "train_set_y")
    val inputLen = trainX.rows

    val parameters = lLayerModel(trainX, trainY, layersDims, learningRate, numIterations, true)
    val accuracyTrain = predictDeep(trainX, trainY.t, parameters)

    val (testX, testY) = readData(TestSetFileName, "test_set_x", "test_set_y")
    val accuracyTest = predictDeep(testX, testY.t, parameters)

    println(s"train accuracy: $accuracyTrain")
    println(s"test accuracy: $accuracyTest")
  }

}
