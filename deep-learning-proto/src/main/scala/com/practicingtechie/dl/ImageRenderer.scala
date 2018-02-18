package com.practicingtechie.dl

import java.awt._
import java.awt.image.BufferedImage
import javax.swing.{JFrame, JPanel}


class ImageRenderer(allImagePixels: Array[Double], imageIndex: Int,
                    imgHeight: Int, imgWidth: Int, imgDepth: Int) extends JPanel {
  import ImageRenderer._
  val pixelsPerImg = imgHeight * imgWidth * imgDepth

  setBackground(Color.white)

  override def paint(g: Graphics): Unit = {
    val g2D = g.asInstanceOf[Graphics2D]
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    val bi = new BufferedImage(imgHeight, imgHeight, BufferedImage.TYPE_INT_RGB)
    val wr = bi.getRaster
    val pixels = allImagePixels.drop(pixelsPerImg*imageIndex).take(pixelsPerImg)
    wr.setPixels(0, 0, imgHeight, imgHeight, pixels)

    g2D.drawImage(bi, 0, 0, imgWidth*ImgScaleFactor, imgHeight*ImgScaleFactor, null)
  }
}


object ImageRenderer {
  val ImgScaleFactor = 6
  val DataSetName = "train_set_x"
  val DataSetFileName = "/Users/aspluma/Downloads/dl-notebook/application/datasets/train_catvnoncat.h5"

  def main(args: Array[String]): Unit = {
    val cdf = ucar.nc2.NetcdfFile.open(DataSetFileName)
    val (shapeX, trainXarr) = readInputData(cdf, DataSetName)
    val (imgHeight, imgWidth, imgDepth) = shapeX match {
      case (_ :: h :: w :: d :: Nil) => (h, w, d)
      case _ => throw new IllegalStateException(s"unexpected shape")
    }
    val imgIndex = args(0).toInt

    val frame1 = new JFrame("2D Images")
    frame1.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE)

    frame1.getContentPane.add("Center", new ImageRenderer(trainXarr, imgIndex, imgHeight, imgWidth, imgDepth))
    frame1.pack()
    frame1.setSize(new Dimension(imgHeight*ImgScaleFactor, imgWidth*ImgScaleFactor))
    frame1.setVisible(true)
  }

}