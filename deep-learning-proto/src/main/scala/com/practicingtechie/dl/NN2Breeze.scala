package com.practicingtechie.dl


object NN2Breeze {
  import collection.JavaConverters._
  import breeze.linalg._, breeze.numerics._

  import com.practicingtechie.dl.Activation._

  val logger = com.typesafe.scalalogging.Logger(this.getClass)

  case class LCache(a: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseVector[Double])
  case class ACache(z: DenseMatrix[Double])
  case class Cache(lc: LCache, ac: ACache)

  val fn = "/Users/aspluma/Downloads/dl-notebook/application/datasets/train_catvnoncat.h5"

  val RandSampler = breeze.stats.distributions.Rand.gaussian

  def initializeParameters(nx: Int, nh: Int, ny: Int) = {
    val w1 = DenseMatrix.rand[Double](nh, nx, RandSampler) * 0.01
    val b1 = DenseVector.zeros[Double](nh)
    val w2 = DenseMatrix.rand[Double](ny, nh, RandSampler) * 0.01
    val b2 = DenseVector.zeros[Double](ny)

    (w1, b1, w2, b2)
  }

  def linearForward(a: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseVector[Double]) = {
    val wa = w * a
    (wa(::, *) + b, LCache(a, w, b))
  }

  def linearActivationForward(aPrev: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseVector[Double], activation: Activation) = {
    val (z, lCache) = linearForward(aPrev, w, b)
    val a = activation match {
      case Sigmoid => sigmoid(z)
      case ReLu => z.map(e => Math.max(0, e))
    }
    (a, Cache(lCache, ACache(z)))
  }

  def computeCost(aL: DenseMatrix[Double], y: DenseVector[Double]): Double = {
    val c = log(aL(0, ::)) * y + log(aL.map(e => 1.0 - e).apply(0, ::)) * y.map(e => 1.0 - e)
    val r = (-1.0 / y.length) * c
    r
  }

  def linearBackward(dZ: DenseMatrix[Double], aPrev: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseVector[Double]) = {
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

  def recalculateParameters(weights: List[(DenseMatrix[Double], DenseMatrix[Double])],
                            biases: List[(DenseVector[Double], DenseVector[Double])],
                            learningRate: Double) = {
    import breeze.linalg.operators.OpSub
    import breeze.linalg.support.CanMapValues
    def recalcParams[M[_], N](parsWithGrads: List[(M[N], M[N])], alpha: N)(
      implicit minus: OpSub.Impl2[M[N], M[N], M[N]], map: CanMapValues[M[N], N, N, M[N]], n: Numeric[N]) =
      parsWithGrads map {
        case (param, gradient) =>
          minus(param, map(gradient, e => n.times(e, alpha)))
      }

    recalcParams(weights, learningRate) zip recalcParams(biases, learningRate)
  }

  def twoLayerModel(x: DenseMatrix[Double], y: DenseVector[Double], layersDims: (Int, Int, Int),
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
        val dA2 = - (a2.map(e => 1.0 / e).apply(*, ::) * y
          - a2.map(e => 1.0 / (1.0 - e)).apply(*, ::) * y.map(e => 1.0 - e))
        val (dA1, dW2, db2) = linearActivationBackward(dA2, cache2, Sigmoid)
        val (dA0, dW1, db1) = linearActivationBackward(dA1, cache1, ReLu)

        recalculateParameters(List((w1, dW1), (w2, dW2)), List((b1, db1), (b2, db2)), learningRate) match {
          case List((nw1, nb1), (nw2, nb2)) => (nw1, nb1, nw2, nb2)
        }
    }
  }

  def predict(x: DenseMatrix[Double], w1: DenseMatrix[Double], b1: DenseVector[Double],
              w2: DenseMatrix[Double], b2: DenseVector[Double]): DenseMatrix[Double] = {
    val (a1, _) = linearForward(x, w1, b1)
    val (a2, _) = linearForward(a1, w2, b2)

    a2.map(p => if (p > 0.5) 1.0 else 0.0)
  }

  def readData(fileName: String, xName: String, yName: String) = {
    val cdf = ucar.nc2.NetcdfFile.open(fileName)
    val (shapeX, xArr) = readInputData(cdf, xName)
    val inputLen = shapeX.drop(1).reduce(_ * _)
    0.until(xArr.size).foreach(i => xArr.update(i, xArr(i) / 255.0))
    val x = new DenseMatrix(inputLen, shapeX.head, xArr)
    println(s"X dims: ${x.rows} ${x.cols}")

    val y = new DenseVector(readLabels(cdf, yName))
    println(s"y dims: ${y.length}")
    cdf.close

    (x, y)
  }

  def main(args: Array[String]): Unit = {
    val (hiddenNodes, outputNodes) = (7, 1)
    val learningRate = 0.0075
    val numIterations = 2500
    val TestSetFileName = "/Users/aspluma/Downloads/dl-notebook/application/datasets/test_catvnoncat.h5"

    val (trainX, trainY) = readData(fn, "train_set_x", "train_set_y")
    val inputLen = trainX.rows

    val (w1, b1, w2, b2) = twoLayerModel(trainX, trainY, (inputLen, hiddenNodes, outputNodes), learningRate, numIterations, true)

    val (testX, testY) = readData(TestSetFileName, "test_set_x", "test_set_y")
    val predictions = predict(testX, w1, b1, w2, b2)
    val accuracy = sum((testY.toDenseMatrix :== predictions).map(v => if(v) 1.0 else 0.0)) / testY.length.toDouble
    println(s"accuracy: $accuracy")
  }
}