package com.dong.tools.tida

import redis.api.scripting.RedisScript
import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._
import redis.protocol.Integer

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  implicit val akkaSystem = akka.actor.ActorSystem()
  implicit val redis = RedisClient()

  val xyz: Decayer = new LinerDecayer(10 minutes)
  val key = "gxaa"
  val f = for {

    w <- xyz.getValue(key, 0)
    _ = println("weight: " + w)
    w <- xyz.addValue(key, 100000, 0)
    _ = println("weight: " + w)
    w <- xyz.getValue(key, 5 * 60 * 1000)
    _ = println("weight: " + w)
    w <- xyz.getValue(key, 10 * 60 * 1000)
    _ = println("weight: " + w)
  } yield ({})

  Await.result(f, 5 seconds)

  akkaSystem.shutdown()
}