name := "vw"

version := "0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "com.google.protobuf" % "protobuf-java" % "2.6.1",
  "commons-codec" % "commons-codec" % "1.10"
)