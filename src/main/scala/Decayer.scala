package com.dong.tools.tida

import scala.concurrent.Future

trait Decayer {
  def add(key: String, value: Long, time: Long = System.currentTimeMillis): Future[Long]
  def addIfSmallerThan(key: String, value: Long, threshold: Long, time: Long = System.currentTimeMillis): Future[Long]
  def get(key: String, time: Long = System.currentTimeMillis): Future[Long]
}