package com.dong.qard

import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  implicit val akkaSystem = akka.actor.ActorSystem()
  implicit val redis = RedisClient()

  val xyz: Decayer = new HalflifeDecayer(10 minutes)
  val key = "gxaa"
  val f = for {

    w <- xyz.get(key, 0)
    _ = println("weight: " + w)
    w <- xyz.add(key, 100000, 0)
    _ = println("weight: " + w)
    w <- xyz.get(key, 5 * 60 * 1000)
    _ = println("weight: " + w)
    w <- xyz.get(key, 10 * 60 * 1000)
    _ = println("weight: " + w)
  } yield ({})

  Await.result(f, 5 seconds)

  akkaSystem.shutdown()
}