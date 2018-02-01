package com.practicingtechie.dl

object NN2nd4j {
  import org.nd4j.linalg.api.ndarray.INDArray
  import org.nd4j.linalg.factory.Nd4j
  import org.nd4j.linalg.api.buffer.DataBuffer
  import org.nd4j.linalg.ops.transforms.Transforms.{sigmoid, relu, log, exp}

  //Nd4j.getRandom().setSeed(1)

  import com.practicingtechie.dl.Activation._

  val logger = com.typesafe.scalalogging.Logger(this.getClass)

  case class LCache(a: INDArray, w: INDArray, b: INDArray) {
    def tupled = (a, w, b)
  }
  case class ACache(z: INDArray)
  case class Cache(lc: LCache, ac: ACache)

  val fn = "/Users/marko/Downloads/dl-notebook/application/datasets/train_catvnoncat.h5"

  def initializeParameters(nx: Int, nh: Int, ny: Int) = {
    val (w1, b1, w2, b2) = (
      Nd4j.randn(nh, nx).mul(0.01),
      Nd4j.zeros(nh, 1),
      Nd4j.randn(ny, nh).mul(0.01),
      Nd4j.zeros(ny, 1))
    (w1, b1, w2, b2)
  }

  def linearForward(a: INDArray, w: INDArray, b: INDArray) = {
    (w.mmul(a).addColumnVector(b), LCache(a, w, b))
  }

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

    val v = y.mmul(log(aL.transpose())).add(
      y.rsub(1).mmul(log(aL.transpose().rsub(1))))
    val cost = (-1.0/m) * v.getDouble(0)
    cost
  }

  def linearBackward(dZ: INDArray, lc: LCache) = {
    val (aPrev, w, b) = lc.tupled
    val m = aPrev.shape()(1)
    val dW = dZ.mmul(aPrev.transpose()).mul(1.0/m)
    val db = dZ.sum(1).mul(1.0/m)
    val dAPrev = w.transpose().mmul(dZ)
    (dAPrev, dW, db)
  }

  def reluBackward(dA: INDArray, z: INDArray) =
    dA.mul(z.gt(0))

  def sigmoidBackward(dA: INDArray, z: INDArray) = {
    val s = exp(z.neg()).add(1).rdiv(1)
    dA.mulRowVector(s).mulRowVector(s.rsub(1))
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
        val dA2 = y.div(a2).sub(y.rsub(1).div(a2.rsub(1))).neg()
        val (dA1, dW2, db2) = linearActivationBackward(dA2, cache2, Sigmoid)
        val (dA0, dW1, db1) = linearActivationBackward(dA1, cache1, ReLu)

        recalculateParameters(List((w1, dW1), (w2, dW2), (b1, db1), (b2, db2)), learningRate) match {
          case nw1 :: nw2 :: nb1 :: nb2 :: _ => (nw1, nb1, nw2, nb2)
          case x => throw new IllegalArgumentException(s"unexpected result: $x")
        }
    }

  }


  def main(args: Array[String]): Unit = {
    Nd4j.setDataType(DataBuffer.Type.DOUBLE)

    val cdf = ucar.nc2.NetcdfFile.open(fn)

    val (dimsX, trainXarr) = readTrainData(cdf, "train_set_x")
    val shapeX: Array[Int] = Array(dimsX.drop(1).reduce(_ * _), dimsX.head.toInt)
    val trainX = Nd4j.create(trainXarr, shapeX, 'c')
    trainX.divi(255.0)
    println(trainX.shape().toList)

    val trainYarr = readLabels(cdf, "train_set_y")
    val trainY = Nd4j.create(trainYarr, Array(1, trainYarr.length), 'c')
    println(trainY.shape().toList)

    cdf.close

    twoLayerModel(trainX, trainY, (12288, 7, 1), 0.0075, 2500, true)
  }

}
