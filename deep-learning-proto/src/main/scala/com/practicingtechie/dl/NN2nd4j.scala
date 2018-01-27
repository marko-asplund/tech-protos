package com.practicingtechie.dl

object NN2nd4j {
  import org.nd4j.linalg.api.ndarray.INDArray
  import org.nd4j.linalg.factory.Nd4j
  import org.nd4j.linalg.api.buffer.DataBuffer
  import org.nd4j.linalg.ops.transforms.Transforms.{sigmoid, relu, log}

  Nd4j.getRandom().setSeed(1)

  import com.practicingtechie.dl.Activation._

  case class LCache(a: INDArray, w: INDArray, b: INDArray)
  case class ACache(z: INDArray)
  case class Cache(lc: LCache, ac: ACache)

  def initializeParameters(nx: Int, nh: Int, ny: Int) = {
    val (w1, b1, w2, b2) = (
      Nd4j.rand(nh, nx).mul(0.01),
      Nd4j.zeros(nh),
      Nd4j.rand(ny, nh).mul(0.01),
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
    cost.sumNumber().asInstanceOf[Double] // ??
  }

  def main(args: Array[String]): Unit = {
    println("hi")
    Nd4j.setDataType(DataBuffer.Type.DOUBLE)

    //Nd4j.create(myDoubleArr,new int[]{10,1})
//    val myDoubleArr = Array[Double](1.0)
//    val z = Nd4j.create(myDoubleArr, Array[Int](10, 1))
//    z

    //initializeParameters()

    val r1 = Nd4j.rand(5, 5)
    println(r1)

    val r2 = Nd4j.create(Array[Float](1, 2, 3, 4), Array[Int](2, 2))
    println(r2)

  }

}
