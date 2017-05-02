name := "qard"

version := "2.0.1"

organization := "io.dong"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8", "2.12.2")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
  Resolver.url("sbt-plugins", url("http://dl.bintray.com/zalando/sbt-plugins"))(Resolver.ivyStylePatterns),
  "jeffmay" at "https://dl.bintray.com/jeffmay/maven")

libraryDependencies ++= Seq(
  "com.github.etaty" %% "rediscala" % "1.8.0",
  "org.specs2" %% "specs2-core" % "3.8.9" % "test")

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org", "3U9tMk68",
  "cohYQ7Ct1ONiS/1T5KrzIrfbdzGXhDCkao8ptPAhGlVo")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}