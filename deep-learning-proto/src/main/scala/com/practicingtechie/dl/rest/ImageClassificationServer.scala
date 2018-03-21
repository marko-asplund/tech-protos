package com.practicingtechie.dl.rest

import cats.effect.IO
import fs2.StreamApp
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.headers.`Content-Type`
import java.io.{File, InputStream}

import concurrent.ExecutionContext.Implicits.global


object ImageClassificationServer extends StreamApp[IO] with Http4sDsl[IO] {

  import com.practicingtechie.dl._
  import com.practicingtechie.dl.NNDeepBreeze._
  import io.circe.syntax._


  val ModelLayersDims = List(12288, 20, 7, 5, 1)
  val ModelLearningRate = 0.0075
  val ModelTrainingIterations = 500

  case class DataSet(name: String, fileName: String, inputSectionName: String, imageCount: Int,
                     imgHeight: Int, imgWidth: Int, imgDepth: Int,
                     accuracy: Double) {
    val datumsPerItem = imgHeight * imgWidth * imgDepth

    def getImage[T](idx: Int, data: Array[T]): Array[T] =
      data.drop(idx * datumsPerItem).take(datumsPerItem)
  }

  object DataSet {
    import io.circe.Encoder

    implicit val encodeUser: Encoder[DataSet] =
      Encoder.forProduct3("name", "imageCount", "accuracy") { ds =>
        (ds.name, ds.imageCount, ds.accuracy)
      }
  }

  case class Prediction(imagePath: String, predictedLabel: Int)

  object Prediction {
    implicit val encoder: Encoder[Prediction] = Encoder.forProduct2("imagePath", "predictedLabel"){ p =>
      p.imagePath -> p.predictedLabel
    }
  }

  val model = initilizeModel(ModelLayersDims, ModelLearningRate, ModelTrainingIterations)

  def initilizeModel(layersDims: List[Int], learningRate: Double, numIterations: Int) = {
    val (trainX, _, trainY) = readDataSet(TrainSetFileName, "train_set_x", "train_set_y")
    lLayerModel(trainX, trainY, layersDims, learningRate, numIterations, true)
  }

  val datasets = {
    def createDataSet(name: String, fn: String, xSection: String, ySection: String) = {
      val (x, shapeX, y) = readDataSet(fn, xSection, ySection)
      val (imageCount, imgHeight, imgWidth, imgDepth) = shapeX match {
        case (cnt :: h :: w :: d :: Nil) => (cnt, h, w, d)
        case _ => throw new IllegalStateException(s"unexpected shape")
      }
      val pixelsPerImg = shapeX.drop(1).reduce(_ * _)
      val accuracy = calculateAccuracy(predictDeep(x, model), y.t)

      DataSet(name, fn, xSection, imageCount, imgHeight, imgWidth, imgDepth, accuracy)
    }

    List(
      createDataSet("train", TrainSetFileName, "train_set_x", "train_set_y"),
      createDataSet("test", TestSetFileName, "test_set_x", "test_set_y")
    )
  }

  val apiService = HttpService[IO] {
    case GET -> Root / "datasets" =>
      Ok(datasets.asJson)
    case GET -> Root / dataset / IntVar(imageId) / "prediction"  =>
      datasets.find(_.name == dataset) match {
        case Some(ds) =>
          val (shape, data) = readDataSetSectionInputNormalized(ds.fileName, ds.inputSectionName)
          val input = inputArrayAsMatrix(ds.getImage(imageId, data), List(1, ds.datumsPerItem))
          val prediction = predictDeep(input, model)

          Ok(Prediction(s"$dataset/$imageId", prediction(0, 0).toInt).asJson)
        case _ => NotFound(s"dataset '$dataset' not found")
      }
    case GET -> Root / dataset / IntVar(imageId) =>
      import javax.imageio.ImageIO
      import java.awt.image.BufferedImage
      val BufferSize = 2048

      datasets.find(_.name == dataset) match {
        case Some(ds) =>
          val (shape, data) = readDataSetSectionInputBytes(ds.fileName, ds.inputSectionName)
          val pixels = ds.getImage(imageId, data.map(java.lang.Byte.toUnsignedInt _))

          val bi = new BufferedImage(ds.imgWidth, ds.imgHeight, BufferedImage.TYPE_INT_RGB)
          bi.getRaster.setPixels(0, 0, ds.imgHeight, ds.imgHeight, pixels)
          val out = new java.io.ByteArrayOutputStream(ds.datumsPerItem)
          ImageIO.write(bi, "jpg", out)

          val is: InputStream = new java.io.ByteArrayInputStream(out.toByteArray)
          IO(Response(body = fs2.io.readInputStream(IO(is), BufferSize))).map(_.withContentType(`Content-Type`(MediaType.`image/jpeg`)))
        case _ => NotFound(s"dataset '$dataset' not found")
      }

  }

  import org.http4s.server.staticcontent
  import org.http4s.server.staticcontent.FileService
  val staticService: HttpService[IO] = staticcontent.fileService(FileService.Config[IO]("front-end/build"))

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(apiService, "/api")
      .mountService(staticService, "/")
      .serve

}
