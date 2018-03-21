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

  def arrayOfByteToDouble(d: Array[Byte]): Array[Double] = d.map(e => java.lang.Byte.toUnsignedInt(e).toDouble)

  def arrayOfByteToDoubleNormalized(d: Array[Byte]): Array[Double] = d.map(e => java.lang.Byte.toUnsignedInt(e) / 255.0)

  val getDataSectionInput = getDataSetSectionInputRaw(arrayOfByteToDouble) _

  def getDataSetSectionInputRaw[T](f: Array[Byte] => Array[T])(cdf: NetcdfFile, name: String): (List[Integer], Array[T]) = {
    val section = cdf.readSection(name)
    val data = f(section.getDataAsByteBuffer.array)
    val shape = asScalaBuffer(intArrayToList(section.getShape)).toList
    (shape, data)
  }

  def readDataSetSectionInput[T](f: NetcdfFile => (List[Integer], Array[T]))(fn: String, name: String): (List[Integer], Array[T]) = {
    val cdf = ucar.nc2.NetcdfFile.open(fn)
    val res = f(cdf)
    cdf.close
    res
  }

  def readDataSetSectionInputBytes(fn: String, name: String): (List[Integer], Array[Byte]) =
    readDataSetSectionInput(getDataSetSectionInputRaw(identity)(_, name))(fn, name)

  def readDataSetSectionInputNormalized(fn: String, name: String) = {
    val cdf = ucar.nc2.NetcdfFile.open(fn)
    val res = getDataSetSectionInputRaw(arrayOfByteToDoubleNormalized)(cdf, name)
    cdf.close
    res
  }

  def getDataSetSectionLabels(cdf: NetcdfFile, name: String): Array[Double] = {
    val section = cdf.readSection(name)
    val data = 0.until(section.getShape()(0)).map(section.getLong(_).toDouble)
    data.toArray
  }

  def readDataSetNormalized(fileName: String, xName: String, yName: String) = {
    val cdf = ucar.nc2.NetcdfFile.open(fileName)

    val (shapeX, xArr) = getDataSetSectionInputRaw(arrayOfByteToDoubleNormalized)(cdf, xName)
    val yArr = getDataSetSectionLabels(cdf, yName)

    cdf.close

    (xArr, shapeX, yArr)
  }

}