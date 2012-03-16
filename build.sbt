
name := "spray-html5-sse"

organization := "eu.getintheloop"

scalaVersion := "2.9.1"

scalacOptions := Seq("-deprecation", "-encoding", "utf8")

resolvers += "spray repo" at "http://repo.spray.cc/"

libraryDependencies ++= Seq(
  "se.scalablesolutions.akka" % "akka-actor" % "1.3.1" % "compile",
  "cc.spray" % "spray-server" % "0.9.0" % "compile",
  "cc.spray" % "spray-can" % "0.9.3" % "compile",
  "cc.spray" %% "spray-json" % "1.1.1" % "compile",
  // runtime
  "se.scalablesolutions.akka" % "akka-slf4j" % "1.3.1" % "runtime",
  "org.slf4j" % "slf4j-api" % "1.6.4" % "runtime",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime"
)

seq(Revolver.settings: _*)
