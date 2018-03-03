organization  := "com.practicingtechie"

name := "aws-lambda-scala"

version := "0.0.1"

scalaVersion := "2.12.4"

fork := false

scalacOptions := Seq("-feature", "-deprecation", "-encoding", "utf8")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val common = project.settings(libraryDependencies := commonLibs)

lazy val pub = project.dependsOn(common).settings(libraryDependencies := commonLibs ++ publisherLibs)

lazy val sub = project.dependsOn(common).settings(libraryDependencies := commonLibs ++ subscriberLibs)

lazy val root = (project in file("."))
  .aggregate(sub, pub)


val commonLibs = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.rogach" %% "scallop" % "2.1.1",
  "com.typesafe" % "config" % "1.3.2",
  "io.argonaut" %% "argonaut" % "6.2"
)

val subscriberLibs = Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
  "com.amazonaws" % "aws-lambda-java-events" % "2.0.2"
)

val publisherLibs = Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.11.289"
)


assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
