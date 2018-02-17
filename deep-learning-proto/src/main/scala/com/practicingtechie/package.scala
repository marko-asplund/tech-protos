package com.practicingtechie

package object dl {
  import collection.JavaConverters._
  import ucar.nc2.NetcdfFile

  object Activation extends Enumeration {
    type Activation = Value
    val Sigmoid, ReLu = Value
  }

  def intArrayToList(a: Array[Int]) =
    java.util.Arrays.stream(a).boxed().collect(java.util.stream.Collectors.toList())

  def readInputData(cdf: NetcdfFile, name: String): (List[Integer], Array[Double]) = {
    val section = cdf.readSection(name)
    val data = section.getDataAsByteBuffer.array.asInstanceOf[Array[Byte]]
    val shape = asScalaBuffer(intArrayToList(section.getShape)).toList
    (shape, data.map(_.toDouble))
  }

  def readLabels(cdf: NetcdfFile, name: String): Array[Double] = {
    val section = cdf.readSection(name)
    val data = 0.until(section.getShape()(0)).map(section.getLong(_).toDouble)
    data.toArray
  }

}