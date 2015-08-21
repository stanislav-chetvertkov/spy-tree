name := "spytree"

version := "0.01"

organization in ThisBuild := "com.github"

scalaVersion := "2.11.6"

resolvers += "Typesafe repository mwn" at "http://repo.typesafe.com/typesafe/maven-releases/"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % "2.4-M3",

  "com.typesafe.akka" %% "akka-actor" % "2.4-M3",

  "com.typesafe.akka" %% "akka-contrib" % "2.4-M3",

  "org.scalatest" %% "scalatest" % "2.2.4")





