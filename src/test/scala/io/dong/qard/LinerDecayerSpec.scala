package com.dong.qard

import org.specs2.mutable.Specification
import org.specs2.control._
import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._
import org.specs2.specification._

import scala.concurrent.ExecutionContext.Implicits.global

class LinerDecayerSpec extends Specification with AfterAll {

  implicit val akkaSystem = akka.actor.ActorSystem()
  implicit val redis = RedisClient()

  val spread = 10 minutes
  val decayer: Decayer = new LinerDecayer(spread)

  def afterAll = { akkaSystem.shutdown() }

  val rand = new scala.util.Random().nextLong
  def key(str: String) = str + rand

  "LinerDecayer should" >> {
    val sometime = System.currentTimeMillis
    "get and set value as expected" >> {
      val k = key("001")
      Await.result(decayer.get(k, sometime), 5 seconds) == 0
      Await.result(decayer.add(k, 100000, sometime), 5 seconds) == 100000
      Await.result(decayer.get(k, sometime), 5 seconds) == 100000
      Await.result(decayer.get(k, sometime + spread.toMillis / 2), 5 seconds) == 50000
      Await.result(decayer.get(k, sometime + spread.toMillis), 5 seconds) == 0
      Await.result(decayer.get(k, sometime - spread.toMillis / 2), 5 seconds) == 150000
      Await.result(decayer.get(k, sometime - spread.toMillis), 5 seconds) == 200000
      Await.result(decayer.get(k, sometime + spread.toMillis + 1), 5 seconds) == 0
      Await.result(decayer.get(k, sometime + (10000 days).toMillis), 5 seconds) == 0
      Await.result(decayer.get(k, Long.MaxValue), 5 seconds) == 0
    }

    "add minus value and still get 0 value" >> {
      val k = key("002")
      Await.result(decayer.get(k, sometime), 5 seconds) == 0
      Await.result(decayer.add(k, -100000, sometime), 5 seconds) == 0
      Await.result(decayer.get(k, sometime), 5 seconds) == 0
    }

    "add value if current value is smaller than a threshold" >> {
      val k = key("003")
      Await.result(decayer.addIfLessThan(k, 1000, 10, sometime), 5 seconds) == -1
      Await.result(decayer.add(k, 100000, sometime), 5 seconds) == 100000
      Await.result(decayer.addIfLessThan(k, 1000, 10, sometime), 5 seconds) == -1
      Await.result(decayer.addIfLessThan(k, 1000, 100000, sometime), 5 seconds) == -1
      Await.result(decayer.addIfLessThan(k, 1000, 100001, sometime), 5 seconds) == 101000
      Await.result(decayer.addIfLessThan(k, -1000, 100000, sometime), 5 seconds) == -1
      Await.result(decayer.addIfLessThan(k, -10000000, 101001, sometime), 5 seconds) == 0
    }
  }
}