package com.practicingtechie.dl


object NN2 {
  import collection.JavaConverters._
  import breeze.linalg._, breeze.numerics._

  object Activation extends Enumeration {
    type Activation = Value
    val Sigmoid, ReLu = Value
  }
  import Activation._

  case class LCache(a: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseVector[Double])
  case class ACache(z: DenseMatrix[Double])
  case class Cache(lc: LCache, ac: ACache)

  val fn = "/Users/marko/Downloads/dl-notebook/application/datasets/train_catvnoncat.h5"
  def intArrayToList(a: Array[Int]) =
    java.util.Arrays.stream(a).boxed().collect(java.util.stream.Collectors.toList())


  def initializeParameters(nx: Int, nh: Int, ny: Int) = {
    val w1 = DenseMatrix.rand[Double](nh, nx) * 0.01
    val b1 = DenseVector.zeros[Double](nh)
    val w2 = DenseMatrix.rand[Double](ny, nh) * 0.01
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
    val c = log(aL.t) * y + log(aL.map(e => 1 - e).t) * y.map(e => 1 - e)
    (-1.0 / y.length) * sum(c)
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
    dA * s * s.map(e => 1 - e)
  }

  def linearActivationBackward(dA: DenseMatrix[Double], cache: Cache, activation: Activation) = {
    val dZ = activation match {
      case Sigmoid => sigmoidBackward(dA, cache.ac.z)
      case ReLu => reluBackward(dA, cache.ac.z)
    }
    val (daPrev, dw, db) = linearBackward(dZ, cache.lc.a, cache.lc.w, cache.lc.b)
    (daPrev, dw, db)
  }

  def twoLayerModel(x: DenseMatrix[Double], y: DenseVector[Double], layersDims: (Int, Int, Int),
                    learningRate: Double = 0.0075, numIterations: Int = 3000, printCost: Boolean = false) = {
    val m = x.cols
    val (nx, nh, ny) = layersDims
    val (w1, b1, w2, b2) = initializeParameters(nx, nh, ny)

    0 until(numIterations) foreach { i =>
      val (a1, cache1) = linearActivationForward(x, w1, b1, ReLu)
      val (a2, cache2) = linearActivationForward(a1, w2, b2, Sigmoid)
      val cost = computeCost(a2, y)

      val dA2 = - (a2.map(e => 1 / e).apply(::, *) * y
        - a2.map(e => 1 / (1 -e)).apply(::, *) * y.map(e => 1 -e))
      val (dA1, dW2, db2) = linearActivationBackward(dA2, cache2, Sigmoid)
      val (dA0, dW1, db1) = linearActivationBackward(dA1, cache1, ReLu)

      // TODO: update parameters
    }
  }

  def main(args: Array[String]): Unit = {
    val (w1, b1, w2, b2) = initializeParameters(3,2,1)

    val cdf = ucar.nc2.NetcdfFile.open(fn)
    val trainx = cdf.readSection("train_set_x")
    val shapex = asScalaBuffer(intArrayToList(trainx.getShape)).toList
    val data = trainx.getDataAsByteBuffer.array.asInstanceOf[Array[Byte]]
    data.indices.foreach(i => data.update(i, (data(i)/255).toByte))
    val m = new DenseMatrix(shapex(0), shapex.drop(1).reduce(_ * _), data).t
    println(m.rows)
    println(m.cols)

    println(trainx.getDataType)

    val trainy = cdf.readSection("train_set_y")
    println(trainy.getDataType)

    cdf.close
  }
}