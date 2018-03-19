package com.practicingtechie.dl.rest

import cats.effect.IO
import fs2.StreamApp
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import java.io.{ByteArrayInputStream, InputStream}
import org.http4s.headers.`Content-Type`

import scala.concurrent.ExecutionContext.Implicits.global

object ImageClassificationServer extends StreamApp[IO] with Http4sDsl[IO] {

  import com.practicingtechie.dl._
  import com.practicingtechie.dl.NNDeepBreeze._
  import io.circe.syntax._


  val ModelLayersDims = List(12288, 20, 7, 5, 1)
  val ModelLearningRate = 0.0075
  val ModelTrainingIterations = 500

  case class DataSet(name: String, imageCount: Int,
                     imgHeight: Int, imgWidth: Int, imgDepth: Int,
                     accuracy: Double, data: Array[Byte]) {
    def getImage(idx: Int): Array[Byte] = {
      println(s"getimage: $idx, $pixelsPerImg")
      data.drop(idx * pixelsPerImg).take(pixelsPerImg)
    }
    val pixelsPerImg = imgHeight * imgWidth * imgDepth
  }

  object DataSet {
    import io.circe.{ Decoder, Encoder }
    implicit val encodeUser: Encoder[DataSet] =
      Encoder.forProduct3("name", "imageCount", "accuracy")(ds =>
        (ds.name, ds.imageCount, ds.accuracy)
      )
  }

  case class Prediction(imagePath: String, predictedLabel: Int)

  val model = initilizeModel(ModelLayersDims, ModelLearningRate, ModelTrainingIterations)


  def initilizeModel(layersDims: List[Int], learningRate: Double, numIterations: Int) = {
    val (trainX, _, trainY) = readData(TrainSetFileName, "train_set_x", "train_set_y")
    lLayerModel(trainX, trainY, layersDims, learningRate, numIterations, true)
  }

  val datasets = {
    def readDataSet(name: String, fn: String, xSection: String, ySection: String) = {
      val (x, shapeX, y) = readData(fn, xSection, ySection)
      val (imageCount, imgHeight, imgWidth, imgDepth) = shapeX match {
        case (cnt :: h :: w :: d :: Nil) => (cnt, h, w, d)
        case _ => throw new IllegalStateException(s"unexpected shape")
      }
      val pixelsPerImg = shapeX.drop(1).reduce(_ * _)
      val accuracy = calculateAccuracy(predictDeep(x, model), y.t)

      DataSet(name, imageCount, imgHeight, imgWidth, imgDepth, accuracy, readInputDataRaw(fn, xSection)._2)
    }

    List(
      readDataSet("train", TrainSetFileName, "train_set_x", "train_set_y"),
      readDataSet("test", TestSetFileName, "test_set_x", "test_set_y")
    )
  }

//  def getImage(dataSetName: String, imageIndex: Int)

  import org.http4s.implicits._
  //import org.http4s.EntityEncoder.inputStreamEncoder
  val service = HttpService[IO] {
    case GET -> Root / "datasets" =>
      Ok(datasets.asJson)
    case GET -> Root / dataset / IntVar(imageId) / "prediction"  =>
      datasets.find(_.name == dataset) match {
        case Some(ds) =>
          //predictDeep()
          //ds.getImage(1).foo
          //Ok("").map(_.withBodyStream(ds.getImage(imageId)).withContentType(`Content-Type`(MediaType.`image/gif`)))
          //IO(ds.getImage(imageId))

          //Stream(ds.getImage(imageId))

          //val s = fs2.io.readInputStream(ds.getImage(imageId), 2048)
          val is = ds.getImage(imageId)
          //Ok().map(_.withBodyStream(fs2.io.readInputStream(IO(is), 2048)).withContentType(`Content-Type`(MediaType.`image/gif`)))
          //Ok().map(_.withBodyStream(fs2.io.readInputStream(IO(is), 2048)))
          Ok()
        case _ => NotFound()
      }
    case GET -> Root / dataset / IntVar(imageId) =>
      import javax.imageio.ImageIO
      import java.awt.image.BufferedImage
      val BufferSize = 2048

      datasets.find(_.name == dataset) match {
        case Some(ds) =>
          val bi = new BufferedImage(ds.imgWidth, ds.imgHeight, BufferedImage.TYPE_INT_RGB)
          bi.getRaster.setPixels(0, 0, ds.imgHeight, ds.imgHeight, ds.getImage(imageId).map(java.lang.Byte.toUnsignedInt _))
          val out = new java.io.ByteArrayOutputStream(ds.pixelsPerImg)
          ImageIO.write(bi, "jpg", out)

          val is: InputStream = new java.io.ByteArrayInputStream(out.toByteArray) //ds.getImage(imageId)
          IO(Response(body = fs2.io.readInputStream(IO(is), BufferSize))).map(_.withContentType(`Content-Type`(MediaType.`image/jpeg`)))

//          Ok().map(_.withBodyStream(fs2.io.readInputStream(IO(is), 2048)).
//            withContentType(`Content-Type`(MediaType.`image/gif`)))
        case _ => NotFound()
      }

  }

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(service, "/api")
      .serve

}
