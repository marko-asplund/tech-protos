package fi.markoa.proto.mllib

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.clustering.{LDA, DistributedLDAModel, LocalLDAModel, LDAModel}
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.Vector

class LDADemo {
  val AppName = "myApp"
  val Master = "local"
  val DataFileName = "data/sample_lda_data.txt"
  val ModelFileName = "myLDAModel"
  val TopicCount = 3

  val ctx = new SparkContext(new SparkConf().setAppName(AppName).setMaster(Master))

  def trainModel = {
    // Load and parse the data
    val data = ctx.textFile(DataFileName)

    val parsedData = data.map(s => Vectors.dense(s.trim.split(' ').map(_.toDouble)))
    // Index documents with unique IDs
    val corpus = parsedData.zipWithIndex.map(_.swap).cache()

    // Cluster the documents into three topics using LDA
    val ldaModel = new LDA().setK(TopicCount).run(corpus)

    // persist model
    ldaModel.save(ctx, ModelFileName)

    ldaModel
  }

  def getPrediction(docNum: Int) = {
    val dm = DistributedLDAModel.load(ctx, ModelFileName)
    val model = dm.toLocal

    val rawData = ctx.textFile(DataFileName).collect
    val data = Seq((0L, Vectors.dense(rawData(docNum).trim.split(' ').map(_.toDouble))))
    val samples = ctx.makeRDD(data)
    val r = model.topicDistributions(samples)

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
    for (topic <- Range(0, TopicCount)) {
      print("Topic " + topic + ":")
      for (word <- Range(0, ldaModel.vocabSize)) { print(" " + topics(word, topic)); }
      println()
    }
  }

}


object LDADemo {

  def main(args: Array[String]): Unit = {
    val l = new LDADemo
    if (args.length > 0 && args(0) == "train") {
      val m = l.trainModel
      l.dumpTopicsMatrix(m)
    } else {
      val p = l.getPrediction(5)
      l.dumpTopicsAndPrediction(p)
    }
    l.stop
  }

}
