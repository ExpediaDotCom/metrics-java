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
package com.expedia.metrics.metrictank

import java.util.Base64

import com.expedia.metrics.util.Encoder
import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

class MetricPointSerializerTest extends FunSpec with Matchers with GivenWhenThen {

  describe("MetricPointSerializer") {
    val metricPointSerializer = new MetricPointSerializer()
    val serializedMetricPoint = Base64.getDecoder.decode("2cmPRFe2qgagjkAbD7yXf3kh8/impeA/xGNiWwEAAAA=")

    val metricPoint = new MetricPoint(new MetricKey(1, Array(0xd9, 0xc9, 0x8f, 0x44, 0x57, 0xb6, 0xaa, 0x06, 0xa0, 0x8e, 0x40, 0x1b, 0x0f, 0xbc, 0x97, 0x7f).map(_.toByte)), 0.5202212202357678, 1533174724L)

    it("should serialize a MetricPoint") {
      Given("A MetricPoint")

      When("serializing")
      val b = metricPointSerializer.serialize(metricPoint)

      Then("the result should be predictable")
      b should be(serializedMetricPoint)
    }

    it("should deserialize a MetricData") {
      Given("A MetricData")

      When("deserializing")
      val m = metricPointSerializer.deserialize(serializedMetricPoint)

      Then("the result should be predictable")
      m should be(metricPoint)
    }
  }
}
