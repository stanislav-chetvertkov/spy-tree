name := "spytree"

version := "0.1.2-SNAPSHOT"

isSnapshot := true

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


publishTo := {
  val nexus = "http://nexus.dins.ru:8888/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "nexus/content/repositories/snapshots")
  else
    Some("releases"  at nexus + "nexus/content/repositories/releases")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")



