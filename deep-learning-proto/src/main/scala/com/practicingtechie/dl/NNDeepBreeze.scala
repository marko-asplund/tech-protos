package com.practicingtechie.dl

object NNDeepBreeze {
  import collection.JavaConverters._
  import breeze.linalg._
  import breeze.numerics._

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

  val fn = "/Users/aspluma/Downloads/dl-notebook/application/datasets/train_catvnoncat.h5"

  val RandSampler = breeze.stats.distributions.Rand.gaussian

  def initializeParametersDeep(layerDims: List[Int]): Parameters =
    Parameters.tupled(1.until(layerDims.size).toList.map { l =>
      DenseMatrix.rand[Double](layerDims(l), layerDims(l-1), RandSampler) * 0.01 ->
        DenseMatrix.zeros[Double](layerDims(l), 1)
    }.unzip)

  def initializeParameters(nx: Int, nh: Int, ny: Int): (DenseMatrix[Double], DenseMatrix[Double], DenseMatrix[Double], DenseMatrix[Double]) = ???

  def linearForward(a: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseMatrix[Double]) = {
//    println(s"linearForward: a: ${dims(a)} ## w: ${dims(w)} ## b: ${dims(b)}")
    val an = (w * a).apply(::, *) + b(::, 0)
    val r = (an, LCache(a, w, b))
//    println(s"linearForward: ==> ${dims(r._1)}")
    r
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
//    println(s"params: ${p.summary}")
//    println(s"fwd/x: ${dims(x)}")
    val (aPrev, caches) = 0.until(p.layers - 1).toList.foldLeft((x, List.empty[Cache])) {
      case ((aPrev, caches), l) =>
//        println(s"lModelForward: $l, ${dims(p.weights(l))} # ${dims(p.biases(l))}")
        val (a, cache) = linearActivationForward(aPrev, p.weights(l), p.biases(l), ReLu)

        a -> (caches :+ cache)
    }
//    println(s"lModelForward: ${p.weights.size-1}, ${dims(p.weights.last)} # ${dims(p.biases.last)}")
    val (al, cache) = linearActivationForward(aPrev, p.weights.last, p.biases.last, Sigmoid)
    //println(s"lModelForward: ==> ${dims(al)}")

    al -> (caches :+ cache)
  }

  def lModelBackward(al: DenseMatrix[Double], yOrig: DenseMatrix[Double], caches: List[Cache]) = {
    val layers = caches.size
    val m = al.cols
    val y = yOrig.toDenseMatrix.reshape(al.rows, al.cols)
    // dAL = - (np.divide(Y, AL) - np.divide(1 - Y, 1 - AL))
    val dAl = -((y /:/ al) -  y.map(e => 1 - e) /:/ al.map(e => 1 - e))
//    println(s"lModelBackward: y: ${dim(y)} ## al:${dim(al)}")
//    println(s"lModelBackward: dAl: ${dim(dAl)}")
    val (da, dw, db) = linearActivationBackward(dAl, caches.last, Sigmoid)
//    println(s"lModelBackward: grads: ${dim(da)} ## ${dim(dw)} ## ${dim(db)}")
    val gradients = 0.until(layers-1).toList.reverse.foldLeft(List(LGradients(da, dw, db.toDenseMatrix.t))) {
      case (grads, l) =>
        val (da, dw, db) = linearActivationBackward(grads.last.da, caches(l), ReLu)
        grads :+ LGradients(da, dw, db.toDenseMatrix.t)
    }
//    println(s"lModelBackward: ${gradients.size}")

    gradients.reverse
  }

  def recalculateParametersDeep(parameters: Parameters, gradients: List[LGradients], learningRate: Double) = {
//    println(s"recalculateParametersDeep: ${parameters.weights.size} ## ${gradients.size}")
//    println(gradients.map(g => (dims(g.da), dims(g.dw), dims(g.db))))
//    println(parameters.summary)
    Parameters.tupled(parameters.weightsAndBiases.zip(gradients).map {
      case ((weight, bias), grad) =>
//        println(s"recalculateParametersDeep: da: ${dims(grad.da)} ## dw: ${dims(grad.dw)} ## db: ${dims(grad.db)}")
//        println(s"recalculateParametersDeep: w: ${dims(weight)} ## b: ${dims(bias)}")
        (weight - (grad.dw * learningRate)) ->
          (bias - (grad.db * learningRate))
    }.unzip)
  }

  def recalculateParameters(weights: List[(DenseMatrix[Double], DenseMatrix[Double])],
                            biases: List[(DenseMatrix[Double], DenseMatrix[Double])],
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

  def lLayerModel(x: DenseMatrix[Double], y: DenseMatrix[Double], layersDims: List[Int],
                  learningRate: Double = 0.0075, numIterations: Int = 3000, printCost: Boolean = false) = {

    0.until(numIterations).foldLeft(initializeParametersDeep(layersDims)) {
      case (parameters, i) =>
        val (al, caches) = lModelForward(x, parameters)
//        println(s"lLayerModel: al: ${dims(al)}")
        val cost = computeCost(al, y)
        if (printCost && i % 100 == 0)
          println(s"Cost after iteration $i: $cost")
        val gradients = lModelBackward(al, y, caches)

        recalculateParametersDeep(parameters, gradients, learningRate)
    }
  }

  /*
  def twoLayerModel(x: DenseMatrix[Double], y: DenseMatrix[Double], layersDims: (Int, Int, Int),
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
//        val dA2 = - (a2.map(e => 1.0 / e).apply(*, ::) * y
//          - a2.map(e => 1.0 / (1.0 - e)).apply(*, ::) * y.map(e => 1.0 - e))
        val dA2: DenseMatrix[Double] = ??? // FIXME
        val (dA1, dW2, db2) = linearActivationBackward(dA2, cache2, Sigmoid)
        val (dA0, dW1, db1) = linearActivationBackward(dA1, cache1, ReLu)

        recalculateParameters(List((w1, dW1), (w2, dW2)), List((b1, db1.toDenseMatrix), (b2, db2.toDenseMatrix)), learningRate) match {
          case List((nw1, nb1), (nw2, nb2)) => (nw1, nb1, nw2, nb2)
        }
    }
  }
  */

  /*
  def predict(x: DenseMatrix[Double], y: DenseMatrix[Double],
              w1: DenseMatrix[Double], b1: DenseMatrix[Double],
              w2: DenseMatrix[Double], b2: DenseMatrix[Double]): Double = {
    val (a1, _) = linearForward(x, w1, b1)
    val (a2, _) = linearForward(a1, w2, b2)
    val predictions = a2.map(p => if (p > 0.5) 1.0 else 0.0)
    val accuracy = sum((y.toDenseMatrix :== predictions).map(v => if(v) 1.0 else 0.0)) / y.rows.toDouble

    accuracy
  }
  */

  def predictDeep(x: DenseMatrix[Double], y: DenseMatrix[Double], parameters: Parameters): Double = {
    val (al, _) = lModelForward(x, parameters)
    val predictions = al.map(p => if (p > 0.5) 1.0 else 0.0)
    println(s"predictDeep: ${dims(al)} ## ${dims(x)}")
    println(s"predictDeep: ${dims(y)}")
    val accuracy = sum((y.toDenseMatrix :== predictions).map(v => if(v) 1.0 else 0.0)) / y.rows.toDouble

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
    val (hiddenNodes, outputNodes) = (7, 1)
    val layersDims = List(12288, 20, 7, 5, 1)
    val learningRate = 0.0075
    val numIterations = 2400
    val TestSetFileName = "/Users/aspluma/Downloads/dl-notebook/application/datasets/test_catvnoncat.h5"

    val (trainX, trainY) = readData(fn, "train_set_x", "train_set_y")
    val inputLen = trainX.rows

    val parameters = lLayerModel(trainX, trainY, layersDims, learningRate, numIterations, true)
    val accuracyTrain = predictDeep(trainX, trainY.t, parameters)

//    val (w1, b1, w2, b2) = twoLayerModel(trainX, trainY, (inputLen, hiddenNodes, outputNodes), learningRate, numIterations, true)
//    val accuracyTrain = predict(trainX, trainY, w1, b1, w2, b2)

    val (testX, testY) = readData(TestSetFileName, "test_set_x", "test_set_y")
    //val accuracyTest = predict(testX, testY, w1, b1, w2, b2)
    val accuracyTest = predictDeep(testX, testY.t, parameters)

    println(s"train accuracy: $accuracyTrain")
    println(s"test accuracy: $accuracyTest")
  }

}
