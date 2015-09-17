
package fi.marko.proto.vw

import java.io.{InputStream, BufferedInputStream, FileInputStream}
import com.google.protobuf.CodedInputStream


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

object VWModelReader {

  val VersionRe = """(\d+)\.(\d+)\.(\d+)""".r
  val FirstSupportedVersion = Version(7, 10, 3)

  def readHeader(inputStream: InputStream): Option[VwModelFileHeader] = {
    val is = CodedInputStream.newInstance(inputStream)
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

  def main(args: Array[String]): Unit = {
    val is = new BufferedInputStream(new FileInputStream(args(0)))
    val h = readHeader(is)
    println(h)
    is.close

  }

}

