name := "tida"

version := "2.0.0-SNAPSHOT"

organization := "io.dong.tools"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8", "2.12.2")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
  Resolver.url("sbt-plugins", url("http://dl.bintray.com/zalando/sbt-plugins"))(Resolver.ivyStylePatterns),
  "jeffmay" at "https://dl.bintray.com/jeffmay/maven")

libraryDependencies ++= Seq(
  "redis.clients" % "jedis" % "2.1.0",
  "com.github.etaty" %% "rediscala" % "1.8.0")