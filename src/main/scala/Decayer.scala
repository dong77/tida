package com.dong.tools.tida

import scala.concurrent.Future

trait Decayer {
  def addValue(key: String, value: Long, time: Long): Future[Long]
  def getValue(key: String, time: Long): Future[Long]
}