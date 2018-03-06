package com.practicingtechie.dl

object NN2nd4j {
  import org.nd4j.linalg.api.ndarray.INDArray
  import org.nd4j.linalg.factory.Nd4j
  import org.nd4j.linalg.api.buffer.DataBuffer
  import org.nd4j.linalg.indexing.conditions._

  import com.practicingtechie.dl.Activation._
  import com.practicingtechie.dl.Nd4jUtil._

  //Nd4j.getRandom().setSeed(1)

  val logger = com.typesafe.scalalogging.Logger(this.getClass)

  val initializeParameters = initializeParametersRandom _
 //val initializeParameters = initializeParametersFromFile _

  def initializeParametersRandom(nx: Int, nh: Int, ny: Int) = {
    val (w1, b1, w2, b2) = (
      Nd4j.randn(nh, nx).muli(0.01),
      Nd4j.zeros(nh, 1),
      Nd4j.randn(ny, nh).muli(0.01),
      Nd4j.zeros(ny, 1))
    (w1, b1, w2, b2)
  }

  def initializeParametersFromFile(nx: Int, nh: Int, ny: Int) = {
    val fromTsv = matrixFromTsv("dev_data/2-layer") _
    val (w1, b1, w2, b2) = (fromTsv("w1.tsv"), fromTsv("b1.tsv"), fromTsv("w2.tsv"), fromTsv("b2.tsv"))

    (w1, b1, w2, b2)
  }

  def recalculateParameters(parsWithGrads: List[(INDArray, INDArray)], alpha: Double) =
    parsWithGrads map {
      case (param, gradient) =>
        param.sub(gradient.mul(alpha))
    }

  def twoLayerModel(x: INDArray, y: INDArray, layersDims: (Int, Int, Int),
                    learningRate: Double = 0.0075, numIterations: Int = 3000, printCost: Boolean = false) = {

    val (nx, nh, ny) = layersDims

    0.until(numIterations).foldLeft(initializeParameters(nx, nh, ny)) {
      case (((w1, b1, w2, b2), i)) =>
        val (a1, cache1) = linearActivationForward(x, w1, b1, ReLu)
        val (a2, cache2) = linearActivationForward(a1, w2, b2, Sigmoid)
        val cost = computeCost(a2, y)
        if (printCost && i % 100 == 0)
          println(s"Cost after iteration $i: $cost")

        // dA2 = - (np.divide(Y, A2) - np.divide(1 - Y, 1 - A2))
        val dA2 = y.div(a2).subi(y.rsub(1).divi(a2.rsub(1))).negi()
        val (dA1, dW2, db2) = linearActivationBackward(dA2, cache2, Sigmoid)
        val (dA0, dW1, db1) = linearActivationBackward(dA1, cache1, ReLu)

        recalculateParameters(List((w1, dW1), (w2, dW2), (b1, db1), (b2, db2)), learningRate) match {
          case nw1 :: nw2 :: nb1 :: nb2 :: _ => (nw1, nb1, nw2, nb2)
          case x => throw new IllegalArgumentException(s"unexpected result: $x")
        }
    }

  }

  def predict(x: INDArray, y: INDArray, w1: INDArray, b1: INDArray, w2: INDArray, b2: INDArray): Double = {
    val (a1, _) = linearActivationForward(x, w1, b1, ReLu)
    val (a2, _) = linearActivationForward(a1, w2, b2, Sigmoid)
    val predictions = a2.cond(new GreaterThan(0.5))
    val accuracy = y.sub(predictions).cond(new EqualsCondition(0.0)).sumNumber().doubleValue / y.columns

    accuracy
  }

  def main(args: Array[String]): Unit = {
    val (hiddenNodes, outputNodes) = (7, 1)
    val learningRate = 0.0075
    val numIterations = 2500

    Nd4j.setDataType(DataBuffer.Type.DOUBLE)

    val (trainX, trainY) = readData(TrainSetFileName, "train_set_x", "train_set_y")
    val inputLen = trainX.shape()(0)

    val (w1, b1, w2, b2) = twoLayerModel(trainX, trainY, (inputLen, hiddenNodes, outputNodes), learningRate, numIterations, true)
    val accuracyTrain = predict(trainX, trainY, w1, b1, w2, b2)

    val (testX, testY) = readData(TestSetFileName, "test_set_x", "test_set_y")
    val accuracyTest = predict(testX, testY, w1, b1, w2, b2)

    println(s"train accuracy: $accuracyTrain")
    println(s"test accuracy: $accuracyTest")
  }

}
