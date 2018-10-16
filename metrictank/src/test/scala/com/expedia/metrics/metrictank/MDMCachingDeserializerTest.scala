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

import java.nio.ByteBuffer
import java.util.Base64

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

class MDMCachingDeserializerTest extends FunSpec with Matchers with GivenWhenThen {
  it("should return null for a MetricPoint with an unseen id") {
    Given("a serialized metricpoint")
    val deserializer = new MDMCachingDeserializer()
    val metricPointBytes = Base64.getDecoder.decode("AiDHJxd7d66Jvz/JXQ/Dv6RALTAqVhTfi1ugei8BAAAA")

    When("deserialising the metric point")
    val metricPoint = deserializer.deserialize(ByteBuffer.wrap(metricPointBytes))

    Then("the metric point should be null")
    metricPoint should be(null)
  }

  describe("MDMCachingDeserializer") {
    it("should be able to deserialise a MetricPoint if the MetricData has been seen") {
      Given("a serialized metric data and metricpoint with the same id")
      val deserializer = new MDMCachingDeserializer()
      val metricDataBytes = Base64.getDecoder.decode("iaJJZNkiMS4yMGM3MjcxNzdiNzdhZTg5YmYzZmM5NWQwZmMzYmZhNKVPcmdJZAGkTmFtZdm0ZXdldGVzdC51cy13ZXN0LTIuc3RhdHMuZ2F1Z2VzLmV3cy1ib29raW5nLXNlcnZpY2UuMzJlNDBkLTUwNzEwN2IyNDliOC5tZXRyaWNzLnZmb3AuaG90ZWxzLmJvb2suRVdTQm9va2luZ1Rlc3RfUElJRExFU1MuRUNPTS43MC44MDgwYTAwYV81Yzk1XzQ5YjBfYjA0NF8wM2M4YTkyNjMwMTAuOTk5dGhQZXJjZW50aWxlqEludGVydmFsHqVWYWx1ZctALTAqVhTfi6RVbml0p3Vua25vd26kVGltZdJboHovpU10eXBlpWdhdWdlpFRhZ3OQ")
      val metricPointBytes = Base64.getDecoder.decode("AiDHJxd7d66Jvz/JXQ/Dv6RALTAqVhTfi1ugei8BAAAA")

      When("deserialising the metric data and then the metric point")
      val metricData = deserializer.deserialize(ByteBuffer.wrap(metricDataBytes))
      val metricPoint = deserializer.deserialize(ByteBuffer.wrap(metricPointBytes))

      Then("the metric point is mapped to the metric definition from the metric data")
      metricPoint should not be null
      metricPoint.getMetricDefinition should be(metricData.getMetricDefinition)
    }
  }
}
