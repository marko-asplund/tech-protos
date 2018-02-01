organization  := "com.practicingtechie"

name := "deep_learning_proto"

version := "0.0.1"

scalaVersion := "2.12.4"

fork := true

run / scalacOptions := Seq("-feature", "-deprecation", "-encoding", "utf8")

javaOptions += "-Djava.library.path=lib"


libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
//  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.rogach" %% "scallop" % "2.1.1",
  "org.scalanlp" %% "breeze" % "1.0-RC2",
  "org.scalanlp" % "breeze-natives_2.12" % "1.0-RC2",
  "commons-io" % "commons-io" % "2.6",
  "org.nd4j" % "nd4j-api" % "0.9.1",
  "org.nd4j" % "nd4j-native-platform" % "0.9.1",
  "org.nd4j" % "nd4j-native" % "0.9.1" classifier "macosx-x86_64",
  "org.specs2" %% "specs2-core" % "4.0.2" % "test"
)

