logLevel := Level.Info

resolvers ++= Seq(
  Resolver.url("sbt-plugins", url("http://dl.bintray.com/zalando/sbt-plugins"))(Resolver.ivyStylePatterns),
  "zalando-bintray" at "https://dl.bintray.com/zalando/maven",
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  Resolver.sonatypeRepo("releases"),
  Resolver.typesafeRepo("releases"))

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.2.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.0.1")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("com.orrsella" % "sbt-sublime" % "1.1.1")
