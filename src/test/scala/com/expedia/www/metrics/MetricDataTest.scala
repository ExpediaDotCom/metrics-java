package com.expedia.www.metrics

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}
import scala.collection.JavaConverters._

class MetricDataTest extends FunSpec with Matchers with GivenWhenThen  {
  describe("a MetricData") {
    it("should have a predictable id") {
      When("creating a MetricData")
      val tags = List("hostname=phall-sandbox")
      val metric = new MetricData(1, "dummy", 60, Math.random, "P", System.currentTimeMillis / 1000L, "gauge", tags.asJava)

      Then("the id should be predictable")
      metric.id should be("1.35fe936049e9cf9fc0453685dc3ba566")
    }
  }
}
