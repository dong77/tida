package com.dong.qard

import redis.api.scripting.RedisScript
import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._
import redis.protocol.Integer

class LinerDecayer(spread: Duration)(
    implicit
    ec: ExecutionContext, redis: RedisClient
) extends Decayer {

  private def scriptFromResource(resource: String) = {
    val file = getClass.getResource(resource).getFile
    val content = scala.io.Source.fromFile(file).mkString
    RedisScript(content)
  }

  private val getValueScript = scriptFromResource("/liner_get_value.lua")
  private val addValueScript = scriptFromResource("/liner_add_value.lua")
  private val addValueIfSmallerThanScript = scriptFromResource("/liner_add_value_ist.lua")

  private val spreadMillisAsString = spread.toMillis.toString

  def add(key: String, value: Long, time: Long = System.currentTimeMillis): Future[Long] = {
    redis.evalshaOrEval(
      addValueScript,
      Seq(key),
      Seq(
        spreadMillisAsString,
        time.toString,
        value.toString
      )
    ).map(_ match {
        case v: Integer => v.toLong
        case _ => throw new Exception("Integer reply expected!")
      })
  }

  def addIfSmallerThan(key: String, value: Long, threshold: Long, time: Long = System.currentTimeMillis): Future[Long] = {
    redis.evalshaOrEval(
      addValueIfSmallerThanScript,
      Seq(key),
      Seq(
        spreadMillisAsString,
        time.toString,
        value.toString,
        threshold.toString
      )
    ).map(_ match {
        case v: Integer => v.toLong
        case _ => throw new Exception("Integer reply expected!")
      })
  }

  def get(key: String, time: Long = System.currentTimeMillis): Future[Long] = {
    redis.evalshaOrEval(
      getValueScript,
      Seq(key),
      Seq(
        spreadMillisAsString,
        time.toString
      )
    ).map(_ match {
        case v: Integer => v.toLong
        case _ => throw new Exception("Integer reply expected!")
      })
  }
}