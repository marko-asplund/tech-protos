package com.practicingtechie.dl

object NN2nd4j {
  import org.nd4j.linalg.api.ndarray.INDArray
  import org.nd4j.linalg.factory.Nd4j
  import org.nd4j.linalg.api.buffer.DataBuffer
  import org.nd4j.linalg.ops.transforms.Transforms.{sigmoid, relu, log, exp}

  //Nd4j.getRandom().setSeed(1)

  import com.practicingtechie.dl.Activation._

  case class LCache(a: INDArray, w: INDArray, b: INDArray) {
    def tupled = (a, w, b)
  }
  case class ACache(z: INDArray)
  case class Cache(lc: LCache, ac: ACache)

  def initializeParameters(nx: Int, nh: Int, ny: Int) = {
    val (w1, b1, w2, b2) = (
      Nd4j.randn(nh, nx).mul(0.01),
      Nd4j.zeros(nh),
      Nd4j.randn(ny, nh).mul(0.01),
      Nd4j.zeros(ny))
    (w1, b1, w2, b2)
  }

  def linearForward(a: INDArray, w: INDArray, b: INDArray) =
    (w.mmul(a).add(b), LCache(a, w, b))

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
      y.rsub(1).mmul(aL.transpose().rsub(1)))
    val cost = v.sum(1).mul(-1/m)
    cost.getFloat(0)
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
    dA.mmul(z.gt(0))

  def sigmoidBackward(dA: INDArray, z: INDArray) = {
    val s = exp(z.neg()).add(1).rdiv(1)
    dA.mmul(s).mmul(s.rsub(1))
  }

  def linearActivationBackward(dA: INDArray, cache: Cache, activation: Activation) = {
    val dZ = activation match {
      case Sigmoid => sigmoidBackward(dA, cache.ac.z)
      case ReLu => reluBackward(dA, cache.ac.z)
    }
    val (daPrev, dw, db) = linearBackward(dZ, cache.lc)
    (daPrev, dw, db)
  }

  def main(args: Array[String]): Unit = {
    println("hi")
    Nd4j.setDataType(DataBuffer.Type.DOUBLE)

    //Nd4j.create(myDoubleArr,new int[]{10,1})
//    val myDoubleArr = Array[Double](1.0)
//    val z = Nd4j.create(myDoubleArr, Array[Int](10, 1))
//    z

    //initializeParameters()

    val r1 = Nd4j.randn(5, 5)
    println(r1)

    val r2 = Nd4j.create(Array[Float](1, 2, 3, 4), Array[Int](2, 2))
    println(r2)
    println("***")

    val r3 = Nd4j.randn(1, 5)
    println(r3)
    println(r3.gt(0))


  }

}
