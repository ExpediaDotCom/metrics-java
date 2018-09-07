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

import java.io.IOException
import java.util.Base64

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

class MetricPointDeserializerTest extends FunSpec with Matchers with GivenWhenThen {
  describe("MetricPointKafkaDeserializer") {

    it("should deserialize a byte array to Metric Point") {
      val mpExpected = new MetricPoint(new MetricKey(1,
        Array(98.toByte, 68.toByte, (-87).toByte, (-88).toByte, (-112).toByte, (-16).toByte, (-116).toByte, (-62).toByte,
          30.toByte, 103.toByte, (-106).toByte, (-105).toByte, (-38).toByte, (-50).toByte, 60.toByte, (-86).toByte)),
        0f, 1536278417l)
      Given("byte array representing a Metric Point")
      var bytes = Base64.getDecoder.decode("AmJEqaiQ8IzCHmeWl9rOPKoAAAAAAAAAAJG/kVsBAAAA")
      val des = new MetricPointDeserializer
      When("deserializing")
      val mp = des.deserialize(bytes)
      Then("deserialized value equals metric point")
      assert(mp == mpExpected)
    }

    it("should fail deserialization when byte array size doesn't equal to 33") {
      Given("bytes array of size 32")
      val bytes = Base64.getDecoder.decode("AmJEqaiQ8IzCHmeWl9rOPKoAAAAAAAAAAJG/kVsBAA")
      val des = new MetricPointDeserializer
      When("deserializing")
      val thrown = intercept[IOException] {
        des.deserialize(bytes)
      }
      Then("IOException is thrown with relevant message")
      assert(thrown.getMessage == "Not a MetricPoint format")
    }

    it("should fail deserialization when byte array represents an unknown format") {
      Given("bytes array representing unknown Metric Point format")
      val bytes = Base64.getDecoder.decode("BGJEqaiQ8IzCHmeWl9rOPKoAAAAAAAAAAJG/kVsBAAAA")
      val des = new MetricPointDeserializer
      When("deserializing")
      val thrown = intercept[IOException] {
        des.deserialize(bytes)
      }
      Then("IOException is thrown with relevant message")
      assert(thrown.getMessage == "Not a MetricPoint format")
    }
  }
}
