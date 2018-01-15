package com.practicingtechie.dl

object CatDemo {
  import collection.JavaConverters._
  import breeze.linalg._

  val fn = "/Users/marko/Downloads/dl-notebook/application/datasets/train_catvnoncat.h5"
  def intArrayToList(a: Array[Int]) =
    java.util.Arrays.stream(a).boxed().collect(java.util.stream.Collectors.toList())


  def initializeParameters(nx: Int, nh: Int, ny: Int) = {
    val w1 = DenseMatrix.rand(nh, nx) * 0.01
    val b1 = DenseMatrix.zeros[Int](nh, 1)
    val w2 = DenseMatrix.rand(ny, nh) * 0.01
    val b2 = DenseMatrix.zeros[Int](ny, 1)

    (w1, b1, w2, b2)
  }

  def linearForwardTestCase() = {
    val a = DenseMatrix.rand(3, 2)
    val w = DenseMatrix.rand(1, 3)
    val b = DenseVector.rand(1)

    (a, w, b)
  }

  def linearForward(a: DenseMatrix[Double], w: DenseMatrix[Double], b: DenseVector[Double]) = {
    val m = w * a
    m(::, *) + b
  }

  /*
  def linearActivationForward(aPrev, W: , b, activation: String) = {

  }
  */

  def main(args: Array[String]): Unit = {
    val (w1, b1, w2, b2) = initializeParameters(3,2,1)

    val (a, w, b) = linearForwardTestCase()
    val r = linearForward(a, w, b)

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