package fi.markoa.proto.mllib

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.clustering.{LDA, DistributedLDAModel, LocalLDAModel, LDAModel}
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.Vector
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory


object Transformers {

  def toSparseVectors(vocabularySize: Int, data: RDD[String]): RDD[Vector] = data.map { l =>
    Vectors.sparse(vocabularySize, l.split(" ").drop(1).map { e =>
      e.split(":") match {
        case Array(id, cnt) => (id.toInt, cnt.toDouble)
      }
    })
  }

}

class LDADemo(numberOfTopics: Int, vocabularySize: Int) {
  val logger = Logger(LoggerFactory.getLogger(classOf[LDADemo]))
  val AppName = "myApp"
  val Master = "local"
  val DataFileName = "data/data-head50.txt"
  val ModelFileName = "myLDAModel"

  val ctx = new SparkContext(new SparkConf().setAppName(AppName).setMaster(Master))

  def trainModel = {
    val parsedData = Transformers.toSparseVectors(vocabularySize, ctx.textFile(DataFileName))

    val corpus = parsedData.zipWithIndex.map(_.swap).cache()

    logger.info("LDA training: start")
    val ldaModel = new LDA().setK(numberOfTopics).run(corpus)
    logger.info("LDA training: done")

    ldaModel.save(ctx, ModelFileName)
    logger.info("model saved")

    ldaModel
  }

  def getPrediction(input: String) = {
    // NB: don't load model for each prediction in non-proto code.
    logger.info("load model: start")
    val dm = DistributedLDAModel.load(ctx, ModelFileName)
    val model = dm.toLocal
    logger.info("load model: done")

    val samples = Transformers.toSparseVectors(vocabularySize, ctx.parallelize(Seq(input)))
    val r = model.topicDistributions(samples.zipWithIndex.map(_.swap))
    logger.info("topicDistributions: done")
    (dm, r)
  }

  def stop = ctx.stop

  def dumpTopicsAndPrediction(p: (DistributedLDAModel, RDD[(Long, Vector)])) = {
    val (dm, r) = (p._1, p._2)
    println("model topic distributions")
    for (doc <- dm.topicDistributions) {
      println(s"${doc._1} # ${doc._2}")
    }

    println(s"query topic distributions")
    for (doc <- r) {
      println(s"${doc._1} # ${doc._2}")
    }
  }

  def dumpTopicsMatrix(ldaModel: LDAModel) = {
    // Output topics. Each is a distribution over words (matching word count vectors)
    println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
    val topics = ldaModel.topicsMatrix
    for (topic <- Range(0, numberOfTopics)) {
      print("Topic " + topic + ":")
      for (word <- Range(0, ldaModel.vocabSize)) { print(" " + topics(word, topic)); }
      println()
    }
  }

}


object LDADemo {

  def main(args: Array[String]): Unit = {
    val l = new LDADemo(100, 105000)
    if (args.length > 0 && args(0) == "train") {
      val m = l.trainModel
    } else {
    }
    l.stop
  }

}
