logLevel := Level.Warn

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.3.8")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0" excludeAll ExclusionRule(organization = "com.danieltrinh"))
libraryDependencies += "org.scalariform" %% "scalariform" % "0.1.7"

resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns)
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5")
