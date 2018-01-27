package com.practicingtechie.dl


object NN2Breeze {
  import collection.JavaConverters._
  import breeze.linalg._, breeze.numerics._

  import com.practicingtechie.dl.Activation._

  case class LCache(a: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseVector[Double])
  case class ACache(z: DenseMatrix[Double])
  case class Cache(lc: LCache, ac: ACache)

  val fn = "/Users/marko/Downloads/dl-notebook/application/datasets/train_catvnoncat.h5"
  def intArrayToList(a: Array[Int]) =
    java.util.Arrays.stream(a).boxed().collect(java.util.stream.Collectors.toList())

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

  def updateParameters(pairs: Iterable[(Double, Double)], alpha: Double): Iterable[Double] = pairs.map {
    case (param, gradient) => param - gradient * alpha
  }

  def twoLayerModel(x: DenseMatrix[Double], y: DenseVector[Double], layersDims: (Int, Int, Int),
                    learningRate: Double = 0.0075, numIterations: Int = 3000, printCost: Boolean = false) = {
    import breeze.linalg.operators.OpSub
    import breeze.linalg.support.CanMapValues

    def recalcParams[M[_], N](parsWithGrads: List[(M[N], M[N])], alpha: N)(
      implicit minus: OpSub.Impl2[M[N], M[N], M[N]], map: CanMapValues[M[N], N, N, M[N]], n: Numeric[N]) =
      parsWithGrads map {
        case (param, gradient) =>
          minus(param, map(gradient, e => n.times(e, alpha)))
      }

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

        recalcParams(List((w1, dW1), (w2, dW2)), learningRate) zip recalcParams(List((b1, db1), (b2, db2)), learningRate) match {
          case List((nw1, nb1), (nw2, nb2)) => (nw1, nb1, nw2, nb2)
        }
    }

  }

  def main(args: Array[String]): Unit = {
    val cdf = ucar.nc2.NetcdfFile.open(fn)

    def readTrainData(name: String): (List[Integer], Array[Double]) = {
      val section = cdf.readSection(name)
      val data = section.getDataAsByteBuffer.array.asInstanceOf[Array[Byte]]
      val shape = asScalaBuffer(intArrayToList(section.getShape)).toList
      (shape, data.map(_.toDouble))
    }
    def readLabels(name: String): DenseVector[Double] = {
      val section = cdf.readSection(name)
      val data = 0.until(section.getShape()(0)).map(section.getLong(_).toDouble)
      new DenseVector(data.toArray)
    }

    val (shapeX, trainXarr) = readTrainData("train_set_x")
    0.until(shapeX.reduce(_ * _)).foreach(i => trainXarr.update(i, trainXarr(i) / 255.0))
    val trainX = new DenseMatrix(shapeX.drop(1).reduce(_ * _), shapeX.head, trainXarr)

    val trainY = readLabels("train_set_y")
    cdf.close

    twoLayerModel(trainX, trainY, (12288, 7, 1), 0.0075, 2500, true)

  }
}