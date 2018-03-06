package com.practicingtechie.dl

object NNDeepNd4j {
  import org.nd4j.linalg.api.buffer.DataBuffer
  import org.nd4j.linalg.api.ndarray.INDArray
  import org.nd4j.linalg.factory.Nd4j
  import org.nd4j.linalg.indexing.conditions._
  import org.nd4j.linalg.ops.transforms.Transforms.{exp, log, relu, sigmoid}

  import com.practicingtechie.dl.Activation._
  import com.practicingtechie.dl.Nd4jUtil._

  Nd4j.getRandom().setSeed(1)


  val logger = com.typesafe.scalalogging.Logger(this.getClass)

  val initializeParametersDeep = initializeParametersDeepRandom _
  //val initializeParametersDeep = initializeParametersDeepFromFile _

  def initializeParametersDeepRandom(layerDims: List[Int]): Parameters =
    Parameters.tupled(1.until(layerDims.size).toList.map { l =>
      Nd4j.randn(layerDims(l), layerDims(l-1)).muli(0.01) -> Nd4j.zeros(layerDims(l), 1)
    }.unzip)


  def initializeParametersDeepFromFile(layerDims: List[Int]): Parameters = {
    val fromTsv = matrixFromTsv("dev_data/l-layer") _

    Parameters.tupled(1.to(4).toList.map { i =>
      fromTsv(s"w$i.tsv") -> fromTsv(s"b$i.tsv")
    }.unzip)
  }

  def lModelForward(x: INDArray, p: Parameters) = {
    val (aPrev, caches) = 0.until(p.layers - 1).toList.foldLeft((x, List.empty[Cache])) {
      case ((aPrev, caches), l) =>
        val (a, cache) = linearActivationForward(aPrev, p.weights(l), p.biases(l), ReLu)

        a -> (caches :+ cache)
    }
    val (al, cache) = linearActivationForward(aPrev, p.weights.last, p.biases.last, Sigmoid)

    al -> (caches :+ cache)
  }

  def lModelBackward(al: INDArray, y: INDArray, caches: List[Cache]) = {
    val layers = caches.size
    val dAl = y.div(al).subi(y.rsub(1).divi(al.rsub(1))).negi()
    val (lda, ldw, ldb) = linearActivationBackward(dAl, caches.last, Sigmoid)

    val gradients = 0.until(layers-1).toList.reverse.foldLeft(List(LGradients(lda, ldw, ldb))) {
      case (grads, l) =>
        val (da, dw, db) = linearActivationBackward(grads.last.da, caches(l), ReLu)
        grads :+ LGradients(da, dw, db)
    }

    gradients.reverse
  }

  def recalculateParametersDeep(parameters: Parameters, gradients: List[LGradients], learningRate: Double) =
    Parameters.tupled(parameters.weightsAndBiases.zip(gradients).map {
      case ((weight, bias), grad) =>
        weight.sub(grad.dw.mul(learningRate)) -> bias.sub(grad.db.mul(learningRate))
    }.unzip)

  def lLayerModel(x: INDArray, y: INDArray, layersDims: List[Int],
                  learningRate: Double = 0.0075, numIterations: Int = 3000, printCost: Boolean = false) = {

    0.until(numIterations).foldLeft(initializeParametersDeep(layersDims)) {
      case (parameters, i) =>
        val (al, caches) = lModelForward(x, parameters)
        val cost = computeCost(al, y)
        if (printCost && i % 100 == 0)
          println(s"Cost after iteration $i: $cost")
        val gradients = lModelBackward(al, y, caches)

        recalculateParametersDeep(parameters, gradients, learningRate)
    }
  }

  def predictDeep(x: INDArray, y: INDArray, parameters: Parameters): Double = {
    val (al, _) = lModelForward(x, parameters)
    val predictions = al.cond(new GreaterThan(0.5))
    val accuracy = y.sub(predictions).cond(new EqualsCondition(0.0)).sumNumber().doubleValue / y.columns

    accuracy
  }

  def main(args: Array[String]): Unit = {
    val layersDims = List(12288, 20, 7, 5, 1)
    val learningRate = 0.0075
    val numIterations = 2500

    Nd4j.setDataType(DataBuffer.Type.DOUBLE)

    val (trainX, trainY) = readData(TrainSetFileName, "train_set_x", "train_set_y")
    val inputLen = trainX.shape()(0)

    val parameters = lLayerModel(trainX, trainY, layersDims, learningRate, numIterations, true)
    val accuracyTrain = predictDeep(trainX, trainY, parameters)

    val (testX, testY) = readData(TestSetFileName, "test_set_x", "test_set_y")
    val accuracyTest = predictDeep(testX, testY, parameters)

    println(s"train accuracy: $accuracyTrain")
    println(s"test accuracy: $accuracyTest")
  }

}
