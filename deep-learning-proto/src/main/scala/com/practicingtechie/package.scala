package com.practicingtechie

package object dl {
  import collection.JavaConverters._
  import ucar.nc2.NetcdfFile

  object Activation extends Enumeration {
    type Activation = Value
    val Sigmoid, ReLu = Value
  }

  val TrainSetFileName = "datasets/train_catvnoncat.h5"
  val TestSetFileName = "datasets/test_catvnoncat.h5"

  def intArrayToList(a: Array[Int]) =
    java.util.Arrays.stream(a).boxed().collect(java.util.stream.Collectors.toList())

  def readInputData(cdf: NetcdfFile, name: String): (List[Integer], Array[Double]) = {
    val section = cdf.readSection(name)
    val data = section.getDataAsByteBuffer.array.map(e => java.lang.Byte.toUnsignedInt(e).toDouble)
    val shape = asScalaBuffer(intArrayToList(section.getShape)).toList
    (shape, data)
  }

  def readLabels(cdf: NetcdfFile, name: String): Array[Double] = {
    val section = cdf.readSection(name)
    val data = 0.until(section.getShape()(0)).map(section.getLong(_).toDouble)
    data.toArray
  }

}