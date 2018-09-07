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

package com.expedia.metrics.util

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

class EncoderTest extends FunSpec with Matchers with GivenWhenThen {
  describe("EncoderTest") {

    Given("bytes")
    val bytes = Array(10.toByte, 2.toByte)
    When("encoding the bytes to hex")
    val result = new java.lang.StringBuilder
    Encoder.encodeHex(result, bytes)
    Then("generate hex code")
    result.toString should equal("0a02")
  }
}
