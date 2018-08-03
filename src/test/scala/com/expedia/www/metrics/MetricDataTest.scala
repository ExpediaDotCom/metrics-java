/*
 * Copyright 2018 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
