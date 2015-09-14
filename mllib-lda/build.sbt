name := "mllib-lda"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "org.apache.spark" % "spark-mllib_2.11" % "1.5.0",
  "com.github.fommil.netlib" % "all" % "1.1.2"
)