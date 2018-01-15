organization  := "com.practicingtechie"

name := "deep_learning_proto"

version := "0.0.1"

scalaVersion := "2.12.4"

fork := false

run / scalacOptions := Seq("-feature", "-deprecation", "-encoding", "utf8")

javaOptions += "-Djava.library.path=lib"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.rogach" %% "scallop" % "2.1.1",
  "org.scalanlp" %% "breeze" % "0.13.2",
  "commons-io" % "commons-io" % "2.6"
  //"org.broadinstitute" % "hdf5-java-bindings" % "1.1.0-hdf5_2.11.0"

)

