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
  "org.scalanlp" %% "breeze" % "1.0-RC2",
  "org.scalanlp" % "breeze-natives_2.12" % "1.0-RC2",
  "commons-io" % "commons-io" % "2.6",
  "org.specs2" %% "specs2-core" % "4.0.2" % "test"
)

