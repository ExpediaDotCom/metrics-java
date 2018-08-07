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
package com.expedia.metrics

import java.util.{Base64, Collections}

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

import scala.collection.JavaConverters._

class MessagePackSerializerTest extends FunSpec with Matchers with GivenWhenThen {

  describe("MessagePackSerializer") {
    val messagePackSerializer = new MessagePackSerializer()
    val serializedMetric = Base64.getDecoder.decode("iaJpZNkiMS5kOWM5OGY0NDU3YjZhYTA2YTA4ZTQwMWIwZmJjOTc3ZqZvcmdfaWQBpG5hbWWhYahpbnRlcnZhbDyldmFsdWXLP+ClpvjzIXmkdW5pdKFQpHRpbWXSW2JjxKVtdHlwZaVnYXVnZaR0YWdzkA==")
    val serializedMetricList = Base64.getDecoder.decode("kYmiaWTZIjEuZDljOThmNDQ1N2I2YWEwNmEwOGU0MDFiMGZiYzk3N2amb3JnX2lkAaRuYW1loWGoaW50ZXJ2YWw8pXZhbHVlyz/gpab48yF5pHVuaXShUKR0aW1l0ltiY8SlbXR5cGWlZ2F1Z2WkdGFnc5A=")

    it("should serialize a MetricData") {
      Given("A MetricData")
      val metric = new MetricData(1, "a", 60, 0.5202212202357678, "P", 1533174724L, "gauge", Collections.emptyList())

      When("serializing")
      val b = messagePackSerializer.serialize(metric)

      Then("the result should be predictable")
      b should be(serializedMetric)
    }

    it("should serialize a List of MetricData") {
      Given("A list of MetricData")
      val metrics = List(new MetricData(1, "a", 60, 0.5202212202357678, "P", 1533174724L, "gauge", Collections.emptyList()))

      When("serializing")
      val b = messagePackSerializer.serialize(metrics.asJava)

      Then("the result should be predictable")
      b should be(serializedMetricList)

    }

  }
}
