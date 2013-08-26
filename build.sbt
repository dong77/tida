name := "tida"

version := "1.1.0-SNAPSHOT"

crossScalaVersions := Seq("2.9.1", "2.9.2", "2.10.0", "2.10.2")

libraryDependencies += "redis.clients" % "jedis" % "2.1.0"

libraryDependencies += "org.msgpack" % "msgpack" % "0.6.8"