import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

name := """spytree"""

version := "2.0"

organization in ThisBuild := "com.github"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",

  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")


enablePlugins(JavaAppPackaging)

