name := "spytree"

version := "0.1.0"

organization in ThisBuild := "com.github"

scalaVersion := "2.11.7"

resolvers += "Typesafe repository mwn" at "http://repo.typesafe.com/typesafe/maven-releases/"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

val akkaVersion = "2.4.0"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,

  "com.typesafe.akka" %% "akka-actor" % akkaVersion,

  "com.typesafe.akka" %% "akka-contrib" % akkaVersion,

  "org.scalatest" %% "scalatest" % "2.2.4")





