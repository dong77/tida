package com.dong.qard

import redis.api.scripting.RedisScript
import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._
import redis.protocol.Integer

class HalflifeDecayer(halflife: Duration)(
    implicit
    ec: ExecutionContext, redis: RedisClient
) extends Decayer {

  private def scriptFromResource(resource: String) = {
    val file = getClass.getResource(resource).getFile
    val content = scala.io.Source.fromFile(file).mkString
    RedisScript(content)
  }

  private val getValueScript = scriptFromResource("/halflife_get_value.lua")
  private val addValueScript = scriptFromResource("/halflife_add_value.lua")
  private val addValueIfSmallerThanScript = scriptFromResource("/halflife_add_value_ist.lua")

  private val halfLifeMillisAsString = halflife.toMillis.toString
  private val expireMillisAsString = (halflife.toMillis * 20).toString

  def add(key: String, value: Long, time: Long = System.currentTimeMillis): Future[Long] = {
    redis.evalshaOrEval(
      addValueScript,
      Seq(key),
      Seq(
        halfLifeMillisAsString,
        expireMillisAsString,
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
        halfLifeMillisAsString,
        expireMillisAsString,
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
        halfLifeMillisAsString,
        time.toString
      )
    ).map(_ match {
        case v: Integer => v.toLong
        case _ => throw new Exception("Integer reply expected!")
      })
  }
}