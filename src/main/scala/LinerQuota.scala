package com.dong.tools.tida

import redis.api.scripting.RedisScript
import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._
import redis.protocol.Integer

class LinerQuota(maxQuota: Long, spread: Duration)(
  implicit ec: ExecutionContext, redis: RedisClient) {

  private val decayer: Decayer = new LinerDecayer(spread)

  def addQuota(key: String, quota: Long) = decayer.add(key, -quota)
  def removeQuota(key: String, quota: Long) = decayer.add(key, quota)
  def getQuota(key: String): Future[Long] = decayer.get(key).map { v => Math.max(0, maxQuota - v) }
}