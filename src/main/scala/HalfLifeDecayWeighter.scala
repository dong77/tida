package com.dong.tools.tida

import redis.api.scripting.RedisScript
import redis.protocol.{ Bulk, MultiBulk }
import redis.RedisClient
import scala.concurrent._

// case class Weight(value: Long, timestamp: Long = System.currentTimeMillis)

class Xyz(redis: RedisClient, halfLifeSeconds: Int)(implicit ec: ExecutionContext) {

  private def scriptFromResource(resource: String) = {
    val file = getClass.getResource("/" + resource).getFile
    val content = scala.io.Source.fromFile(file).mkString
    RedisScript(content)
  }

  private val getWeightScript = scriptFromResource("get_weight.lua")
  private val modWeightScript = scriptFromResource("modify_weight.lua")

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
        case b: Bulk => b.toString.toLong
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
        case b: Bulk => b.toString.toLong
        case _ => throw new Exception("Bulk reply expected!")
      })
  }
}
