organization  := "com.practicingtechie"

name := "tsv_to_xls"

version := "1.0"

scalaVersion := "2.12.2"

scalacOptions := Seq("-feature", "-deprecation", "-encoding", "utf8")


libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.rogach" %% "scallop" % "2.1.1",
  "com.github.tototoshi" %% "scala-csv" % "1.3.4",
  "org.apache.poi" % "poi" % "3.16",
  "org.apache.poi" % "poi-ooxml" % "3.16"
)

