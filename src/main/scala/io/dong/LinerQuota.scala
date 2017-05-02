package com.dong.qard

import redis.api.scripting.RedisScript
import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._
import redis.protocol.Integer

class Quota(maxQuota: Long, decayer: Decayer)(
    implicit
    ec: ExecutionContext
) {
  private def toQuota(v: Long) = Math.min(Math.max(0, maxQuota - v), maxQuota)

  def addQuota(key: String, quota: Long) = decayer.add(key, -quota).map(toQuota)
  def removeQuota(key: String, quota: Long) = decayer.add(key, quota).map(toQuota)
  def getQuota(key: String): Future[Long] = decayer.get(key).map(toQuota)
  def removeQuotaIfSuffcient(key: String, quota: Long) = decayer.addIfLessThan(key, quota, maxQuota - quota).map(_ != -1)
}

class LinerQuota(maxQuota: Long, spread: Duration)(
  implicit
  ec: ExecutionContext, redis: RedisClient
) extends Quota(maxQuota, new LinerDecayer(spread))

class HalflifeQuota(maxQuota: Long, halflife: Duration)(
  implicit
  ec: ExecutionContext, redis: RedisClient
) extends Quota(maxQuota, new HalflifeDecayer(halflife))

