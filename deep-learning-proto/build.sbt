organization  := "com.practicingtechie"

name := "deep_learning_proto"

version := "0.0.1"

scalaVersion := "2.12.4"

fork := true

run / scalacOptions := Seq("-feature", "-deprecation", "-encoding", "utf8")

javaOptions += "-Djava.library.path=lib"

resolvers += "Unidata/thredds releases" at "https://artifacts.unidata.ucar.edu/repository/unidata-releases"

val Http4sVersion = "0.18.2"
val BreezeVersion = "1.0-RC2"
val Nd4jVersion = "0.9.1"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
//  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.rogach" %% "scallop" % "2.1.1",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "edu.ucar" % "netcdfAll" % "4.6.11",
  "org.scalanlp" %% "breeze" % BreezeVersion,
  "org.scalanlp" %% "breeze-natives" % BreezeVersion,
  "commons-io" % "commons-io" % "2.6",
  "org.nd4j" % "nd4j-api" % Nd4jVersion,
  "org.nd4j" % "nd4j-native-platform" % Nd4jVersion,
  "org.nd4j" % "nd4j-native" % Nd4jVersion classifier "macosx-x86_64",
  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "io.circe"        %% "circe-generic"       % "0.9.2",
  "io.circe"        %% "circe-literal"       % "0.9.2",
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
  "org.specs2" %% "specs2-core" % "4.0.2" % "test"
)


addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
