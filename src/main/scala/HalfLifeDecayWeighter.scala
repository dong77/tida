package com.dong.tools.tida

import redis.api.scripting.RedisScript
import redis.RedisClient
import scala.concurrent._
import redis.protocol.Integer

class Xyz(redis: RedisClient, halfLifeSeconds: Int)(implicit ec: ExecutionContext) {

  private def scriptFromResource(resource: String) = {
    val file = getClass.getResource("/" + resource).getFile
    val content = scala.io.Source.fromFile(file).mkString
    RedisScript(content)
  }

  private val getWeightScript = scriptFromResource("get_weight.lua")
  private val modWeightScript = scriptFromResource("mod_weight.lua")

  private val halfLifeSecondsAsString = halfLifeSeconds.toString
  private val expireSecondsAsString = (halfLifeSeconds * 20).toString

  def addWeight(key: String, weight: Long, time: Long = System.currentTimeMillis): Future[Long] = {
    redis.evalshaOrEval(
      modWeightScript,
      Seq(key),
      Seq(
        halfLifeSecondsAsString,
        expireSecondsAsString,
        (time / 1000).toString,
        weight.toString
      )
    ).map(_ match {
        case v: Integer => v.toLong
        case _ => throw new Exception("Bulk reply expected!")
      })
  }

  def getWeight(key: String, time: Long = System.currentTimeMillis): Future[Long] = {
    redis.evalshaOrEval(
      getWeightScript,
      Seq(key),
      Seq(
        halfLifeSecondsAsString,
        (time / 1000).toString
      )
    ).map(_ match {
        case v: Integer => v.toLong
        case _ => throw new Exception("Bulk reply expected!")
      })
  }
}

import scala.concurrent.duration._

object Main extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val akkaSystem = akka.actor.ActorSystem()
  val redis = RedisClient()

  val xyz = new Xyz(redis, 5 * 60)
  val key = "aaaz"
  val f = for {

    w <- xyz.getWeight(key, 0)
    _ = println("weight: " + w)
    w <- xyz.addWeight(key, 100000, 0)
    _ = println("weight: " + w)
    w <- xyz.getWeight(key, 5 * 60 * 1000)
    _ = println("weight: " + w)
    w <- xyz.getWeight(key, 10 * 60 * 1000)
    _ = println("weight: " + w)
  } yield ({})

  Await.result(f, 5 seconds)

  akkaSystem.shutdown()
}