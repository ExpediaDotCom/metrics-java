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
package com.expedia.metrics.jackson

import java.nio.charset.StandardCharsets
import java.util.Collections

import com.expedia.metrics.{MetricData, MetricDefinition, TagCollection}
import org.json.{JSONArray, JSONObject}
import org.scalatest.{FunSpec, Matchers}

import scala.collection.JavaConverters._

class JacksonSerializerTest extends FunSpec with Matchers {

  describe("JacksonSerializerTest") {
    val jacksonSerializer = new JacksonSerializer()
    val tags = new TagCollection(Map(
      MetricDefinition.UNIT -> "P",
      MetricDefinition.MTYPE -> "gauge"
    ).asJava)
    val meta = new TagCollection(Map[String, String](
      "tag" -> "value"
    ).asJava)
    val metric = new MetricData(new MetricDefinition(tags, meta), 0.5202212202357678, 1533174724L)
    val metrics = List(metric).asJava

    val metricStr = "{\"metricDefinition\":{\"key\":null,\"tags\":{\"kv\":{\"mtype\":\"gauge\",\"unit\":\"P\"},\"v\":[]},\"meta\":{\"kv\":{\"tag\":\"value\"},\"v\":[]}},\"value\":0.5202212202357678,\"timestamp\":1533174724}"
    val metricJson = new JSONObject(metricStr)
    val metricsStr = "[" + metricStr + "]"
    val metricsJson = new JSONArray(metricsStr)

    val metricWithKey = new MetricData(new MetricDefinition("key", tags, meta), 0.5202212202357678, 1533174724L)
    val metricWithKeyStr = "{\"metricDefinition\":{\"key\":\"key\",\"tags\":{\"kv\":{\"mtype\":\"gauge\",\"unit\":\"P\"},\"v\":[]},\"meta\":{\"kv\":{\"tag\":\"value\"},\"v\":[]}},\"value\":0.5202212202357678,\"timestamp\":1533174724}"
    val metricWithKeyJson = new JSONObject(metricWithKeyStr)

    it("should serialize a MetricData") {
      val data = jacksonSerializer.serialize(metric)
      val serialised = new JSONObject(new String(data))
      assert(serialised.similar(metricJson))
    }

    it("should serialize a MetricData with a key") {
      val data = jacksonSerializer.serialize(metricWithKey)
      val serialised = new JSONObject(new String(data))
      assert(serialised.similar(metricWithKeyJson))
    }

    it("should serialize a list of MetricData") {
      val data = jacksonSerializer.serializeList(metrics)
      val serialised = new JSONArray(new String(data))
      assert(serialised.similar(metricsJson))
    }

    it("should deserialize a MetricData") {
      val deserialized = jacksonSerializer.deserialize(metricStr.getBytes(StandardCharsets.UTF_8))
      deserialized should be(metric)
    }

    it("should deserialize a MetricData with a key") {
      val deserialized = jacksonSerializer.deserialize(metricWithKeyStr.getBytes(StandardCharsets.UTF_8))
      deserialized should be(metricWithKey)
    }

    it("should deserialize a list of MetricData") {
      val deserialized = jacksonSerializer.deserializeList(metricsStr.getBytes(StandardCharsets.UTF_8))
      deserialized should be(metrics)
    }

  }
}
