package com.dongw.tida

import redis.clients.jedis._

case class Weight(value: Int, timestampMillis: Long = System.currentTimeMillis())

class HalfLifeDecayWeighter(pool: JedisPool, halfLifeMinutes: Int) {
  private val halfLifeSecondsAsString = (halfLifeMinutes * 60).toString
  private val expireSecondsAsString = (halfLifeMinutes * 60 * 10).toString
  private val addWeightSHA = loadLuaResource("add_weight.lua")
  private val singleReadWeightSHA = loadLuaResource("single_read_weight.lua")

  def addWeight(key: String, weight: Weight): Int =
    borrow { jedis =>
      try {
        jedis.evalsha(addWeightSHA,
          1,
          key, // keys to save
          halfLifeSecondsAsString,
          expireSecondsAsString,
          (weight.timestampMillis / 1000).toString, // current time in second
          weight.value.toString).toString().toInt
      } catch {
        case _: Throwable => 0
      }
    }

  def getWeight(key: String, timestampMillis: Long = System.currentTimeMillis()): Int =
    borrow { jedis =>
      try {
        jedis.evalsha(singleReadWeightSHA,
          1,
          key,
          halfLifeSecondsAsString,
          (timestampMillis / 1000).toString).toString().toInt
      } catch {
        case _: Throwable => 0
      }
    }

  private def loadLuaResource(fileName: String): String =
    borrow { jedis =>
      val file = getClass.getResource("/" + fileName).getFile
      val content = scala.io.Source.fromFile(file).mkString

      val sha = jedis.scriptLoad(content)
      println("Lua script loaded: " + sha)
      sha
    }

  private def borrow[T](method: Jedis => T): T = {
    val jedis = pool.getResource()
    try {
      method(jedis)
    } finally {
      pool.returnResourceObject(jedis)
    }
  }
}