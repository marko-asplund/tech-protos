
package fi.marko.proto.vw

import java.io.{InputStream, BufferedInputStream, FileInputStream}
import com.google.protobuf.CodedInputStream
import scala.annotation.tailrec


case class Version(major: Int, minor: Int, rev: Int) {
  def >=(v: Version): Boolean =
    if (major < v.major) false
    else if (major > v.major) true
    else if (minor < v.minor) false
    else if (minor > v.minor) true
    else if (rev >= v.rev ) true
    else false
}

case class VwModelFileHeader(version: Version, minLabel: Float, maxLabel: Float, bits: Int,
  lda: Int, ngramLen: Int, skipLen: Int, options: String)

case class VwModelData(data: Array[(Int, Seq[Float])])


object VWModelReader {

  val VersionRe = """(\d+)\.(\d+)\.(\d+)""".r
  val FirstSupportedVersion = Version(7, 10, 3)

  def readHeader(is: CodedInputStream): Option[VwModelFileHeader] = {
    val vs = is.readRawBytes(is.readFixed32)
    new String(vs, 0, vs.length - 1) match {
      case VersionRe(major, minor, rev) =>
        val v = Version(major.toInt, minor.toInt, rev.toInt)
        assert(v >= FirstSupportedVersion)
        val m = new String(is.readRawBytes(1))
        val (minLabel, maxLabel) = (is.readFloat, is.readFloat)
        val bits = is.readFixed32
        val lda = is.readFixed32
        val ngramLen = is.readFixed32
        assert(ngramLen == 0)
        val skipLen = is.readFixed32
        assert(skipLen == 0)

        val os = is.readRawBytes(is.readFixed32)
        val opts = new String(os, 0, os.length - 1)

        Some(VwModelFileHeader(v, minLabel, maxLabel, bits, lda, ngramLen, skipLen, opts))
      case _ =>
        None
    }
  }

  def readModel(hdr: VwModelFileHeader, is: CodedInputStream) = {
    val indexes = 1.to(hdr.lda)
    @tailrec
    def go(data: Array[(Int, Seq[Float])]): Array[(Int, Seq[Float])] = {
      is.resetSizeCounter
      if (is.isAtEnd) data
      else go(data :+ (is.readFixed32, indexes.map(i => is.readFloat)))
    }
    VwModelData(go(Array()))
  }

  def main(args: Array[String]): Unit = {
    val is = new BufferedInputStream(new FileInputStream(args(0)))
    val cis = CodedInputStream.newInstance(is)
    val h = readHeader(cis)
    val m = readModel(h.get, cis)

    val row = m.data(100000)
    println(row._1 + ", " + row._2.map(i => "%1.6f" format i))

    println(h)
    is.close
  }

}

