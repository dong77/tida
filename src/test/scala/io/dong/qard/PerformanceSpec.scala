package com.dong.qard

import org.specs2.mutable.Specification
import org.specs2.control._
import redis.RedisClient
import scala.concurrent._
import scala.concurrent.duration._
import org.specs2.specification._

import scala.concurrent.ExecutionContext.Implicits.global

class PerformanceSpec extends Specification with AfterAll {

  implicit val akkaSystem = akka.actor.ActorSystem()
  implicit val redis = RedisClient()

  val spread = 10 minutes
  val decayer: Decayer = new LinerDecayer(spread)

  def afterAll = { akkaSystem.shutdown() }

  val rand = new scala.util.Random().nextLong
  def key(str: String) = str + rand

  def measure(name: String, iteration: Int)(op: Int => Any) {
    val start = System.currentTimeMillis

    (1 to 10000) foreach (op)

    val end = System.currentTimeMillis
    val total = end - start
    val each = total.toFloat / iteration
    println(s"$iteration $name operations cost $total ms, each operation cost $each ms")
  }

  "LinerDecayer should" >> {
    val sometime = System.currentTimeMillis
    "`get` really fast" >> {
      val k = key("001")
      measure("get", 10000) { i =>
        Await.result(decayer.get(k + i, sometime + i), 5 seconds)
      }
      1 == 1
    }

    "`add` really fast" >> {
      val k = key("001")
      measure("add", 10000) { i =>
        Await.result(decayer.add(k + i, 100, sometime + i), 5 seconds)
      }
      1 == 1
    }

    "`add` really fast" >> {
      val k = key("001")
      measure("add+addIfSmallerThan", 10000) { i =>
        Await.result(decayer.add(k + i, 100, sometime + i), 5 seconds)
        Await.result(decayer.addIfSmallerThan(k + i, 100, 1000, sometime + i), 5 seconds)
      }
      1 == 1
    }

  }
}