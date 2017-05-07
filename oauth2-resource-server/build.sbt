organization  := "com.practicingtechie"

name := "oauth"

version := "0.1"

scalaVersion := "2.12.1"

val http4sVersion = "0.15.0a"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.apache.oltu.oauth2" % "org.apache.oltu.oauth2.client" % "1.0.2",
  "commons-codec" % "commons-codec" % "1.10"
)
