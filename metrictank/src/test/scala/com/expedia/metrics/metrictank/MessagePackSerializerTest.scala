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

import java.util.{Base64, Collections}

import com.expedia.metrics.{MetricData, MetricDefinition, TagCollection}
import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

import scala.collection.JavaConverters._

class MessagePackSerializerTest extends FunSpec with Matchers with GivenWhenThen {

  describe("MessagePackSerializer") {
    val messagePackSerializer = new MessagePackSerializer()
    val serializedMetric = Base64.getDecoder.decode("iaJJZNkiMS5kOWM5OGY0NDU3YjZhYTA2YTA4ZTQwMWIwZmJjOTc3ZqVPcmdJZAGkTmFtZaFhqEludGVydmFsPKVWYWx1Zcs/4KWm+PMheaRVbml0oVCkVGltZdMAAAAAW2JjxKVNdHlwZaVnYXVnZaRUYWdzkA==")
    val serializedMetricList = Base64.getDecoder.decode("kYmiSWTZIjEuZDljOThmNDQ1N2I2YWEwNmEwOGU0MDFiMGZiYzk3N2alT3JnSWQBpE5hbWWhYahJbnRlcnZhbDylVmFsdWXLP+ClpvjzIXmkVW5pdKFQpFRpbWXTAAAAAFtiY8SlTXR5cGWlZ2F1Z2WkVGFnc5A=")

    val tags = new TagCollection(Map(
      MessagePackSerializer.ORG_ID -> "1",
      MessagePackSerializer.INTERVAL -> "60",
      MetricDefinition.UNIT -> "P",
      MetricDefinition.MTYPE -> "gauge"
    ).asJava)
    val metric = new MetricData(new MetricDefinition("a", tags, TagCollection.EMPTY), 0.5202212202357678, 1533174724L)
    val metrics = List(metric).asJava

    it("should serialize a MetricData") {
      Given("A MetricData")

      When("serializing")
      val b = messagePackSerializer.serialize(metric)

      Then("the result should be predictable")
      b should be(serializedMetric)
    }

    it("should serialize a List of MetricData") {
      Given("A list of MetricData")

      When("serializing")
      val b = messagePackSerializer.serializeList(metrics)

      Then("the result should be predictable")
      b should be(serializedMetricList)
    }

    it("should deserialize a MetricData") {
      Given("A MetricData")

      When("deserializing")
      val m = messagePackSerializer.deserialize(serializedMetric)

      Then("the result should be predictable")
      m should be(metric)
    }

    it("should deserialize a List of MetricData") {
      Given("A list of MetricData")

      When("deserializing")
      val l = messagePackSerializer.deserializeList(serializedMetricList)

      Then("the result should be predictable")
      l should be(metrics)
    }

    it("should default to an empty unit when deserialising") {
      Given("A MetricData with no unit")
      val serializedMetricNoUnit = Base64.getDecoder.decode("iaJJZNkiMS5mNmJlZTcyZTU1OWI0ZDM4YmMwMWJhZmU5NWE3YjFlZaVPcmdJZAGkTmFtZaFhqEludGVydmFsPKVWYWx1ZctAyAAAAAAAAKRVbml0oKRUaW1l0wAAAABcNBY9pU10eXBlpWdhdWdlpFRhZ3OQ")
      val tagsEmptyUnit = new TagCollection(Map(
        MessagePackSerializer.ORG_ID -> "1",
        MessagePackSerializer.INTERVAL -> "60",
        MetricDefinition.UNIT -> "",
        MetricDefinition.MTYPE -> "gauge"
      ).asJava)
      val metricNoUnit = new MetricData(new MetricDefinition("a", tagsEmptyUnit, TagCollection.EMPTY), 12288, 1546917437L)

      When("deserializing")
      val m = messagePackSerializer.deserialize(serializedMetricNoUnit)

      Then("the result should have a unit that is the empty string")
      m should be(metricNoUnit)
    }

  }
}
