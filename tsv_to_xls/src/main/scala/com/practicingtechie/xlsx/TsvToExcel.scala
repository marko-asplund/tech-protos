package com.practicingtechie.xlsx

import java.io.FileOutputStream

import com.github.tototoshi.csv.TSVFormat
import com.typesafe.scalalogging.Logger
import org.rogach.scallop.ScallopConf
import org.slf4j.LoggerFactory

import util.matching.Regex


object TsvToExcel {
  val logger = Logger(LoggerFactory.getLogger(TsvToExcel.getClass))
  import java.io.File
  val FileEncoding = "utf-8"
  val TsvSplit = "\t"

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val sources = opt[List[String]](required = true)
    val hasHeader = opt[Boolean](default = Some(true))
    val schema = opt[String]()
    val target = trailArg[String]()
    override def verify(): Unit = {
      super.verify()
    }
    def sourceFiles = sources().map(s => new File(s)).filter(_.exists)
    verify()
  }

  object ColDataType extends Enumeration {
    type ColDataType = Value
    val String, Int, Double = Value
  }
  import ColDataType.ColDataType
  case class ColHeader(name: String, dataType: ColDataType)
  case class SchemaItem(colKeyRegex: Regex, dataType: ColDataType)
  val TsvFormat: TSVFormat = new TSVFormat {}

  def main(args: Array[String]): Unit = {
    import org.apache.poi.ss.util.WorkbookUtil
    import org.apache.poi.xssf.usermodel.XSSFWorkbook
    import com.github.tototoshi.csv._

    val conf = new Conf(args)

    require(conf.sourceFiles.nonEmpty, "At least one source file must exist")
    require(!(new File(conf.target()).exists), "Target file must not exist")
    require(new File(conf.schema()).exists, "schema file must exist")

    val schema = io.Source.fromFile(conf.schema()).getLines.toList.map { r =>
      val i = r.split(TsvSplit)
      SchemaItem(i(0).r, ColDataType.withName(i(1)))
    }
    val wb = new XSSFWorkbook()
    val out = new FileOutputStream(conf.target())
    def getColHeaders(baseName: String, cols: List[String]) = {
      cols.map { h =>
        schema.find(_.colKeyRegex.findFirstIn(s"$baseName:$h").isDefined) match {
          case Some(m) => ColHeader(h, m.dataType)
          case _ => ColHeader(h, ColDataType.String)
        }
      }
    }

    conf.sourceFiles foreach { sourceFile =>
      val reader = CSVReader.open(sourceFile)(TsvFormat)
      val lines = reader.iterator.buffered

      val baseName = sourceFile.getName.split("\\.").head
      val headers = if (conf.hasHeader()) {
        getColHeaders(baseName, lines.next.toList)
      } else {
        getColHeaders(baseName, 0.to(lines.head.toList.size).toList.map(i => s"col$i"))
      }
      logger.debug(s"$baseName headers: $headers")
      val sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(sourceFile.getName))
      def writeRow(rowNum: Int, line: Seq[String]) = {
        val row = sheet.createRow(rowNum)
        try {
          line.zipWithIndex foreach {
            case (v, num) if headers(num).dataType == ColDataType.Int =>
              row.createCell(num).setCellValue(v.toInt)
            case (v, num) if headers(num).dataType == ColDataType.Double =>
              row.createCell(num).setCellValue(v.toDouble)
            case (v, num) =>
              row.createCell(num).setCellValue(v)
          }
        } catch {
          case e: Exception =>
            logger.error(s"failed to handle row: $rowNum, $line, $e")
            logger.error(line.mkString(TsvSplit))
        }
      }
      def writeHeaderRow(rowNum: Int) = {
        val row = sheet.createRow(rowNum)
        headers.zipWithIndex foreach { case (v, num) =>
          row.createCell(num).setCellValue(v.name)
        }
      }
      val rowNums = Stream.from(0).iterator

      if (conf.hasHeader())
        writeHeaderRow(rowNums.next)
      lines foreach { line =>
        writeRow(rowNums.next, line)
      }

      reader.close
    }

    wb.write(out)
    out.close

  }
}
