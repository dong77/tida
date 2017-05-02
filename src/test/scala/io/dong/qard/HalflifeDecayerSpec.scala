package com.dong.qard

import org.specs2.mutable.Specification
import org.specs2.control._
import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._
import org.specs2.specification._

import scala.concurrent.ExecutionContext.Implicits.global

class HalflifeDecayerSpec extends Specification with AfterAll {

  implicit val akkaSystem = akka.actor.ActorSystem()
  implicit val redis = RedisClient()

  val halflife = 10 minutes
  val decayer: Decayer = new HalflifeDecayer(halflife)

  def afterAll = { akkaSystem.shutdown() }

  val rand = new scala.util.Random().nextLong
  def key(str: String) = str + rand

  "HalflifeDecayer should" >> {
    val sometime = System.currentTimeMillis
    "get and set value as expected" >> {
      val k = key("001")
      Await.result(decayer.get(k, sometime), 5 seconds) == 0
      Await.result(decayer.add(k, 100000, sometime), 5 seconds) == 100000
      Await.result(decayer.get(k, sometime), 5 seconds) == 100000
      Await.result(decayer.get(k, sometime + halflife.toMillis * 1), 5 seconds) == 50000
      Await.result(decayer.get(k, sometime + halflife.toMillis * 2), 5 seconds) == 25000
      Await.result(decayer.get(k, sometime + halflife.toMillis * 3), 5 seconds) == 12500
      Await.result(decayer.get(k, sometime - halflife.toMillis * 1), 5 seconds) == 200000
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