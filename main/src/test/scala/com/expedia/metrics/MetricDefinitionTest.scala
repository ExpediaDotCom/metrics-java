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

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}
import scala.collection.JavaConverters._

class MetricDefinitionTest extends FunSpec with Matchers  {
  describe("a MetricDefinition") {
    it("should require a unit") {
      val tags = Map("mtype" -> "gauge")
      val meta = Map[String, String]()
      an [IllegalArgumentException] should be thrownBy new MetricDefinition(tags.asJava, meta.asJava)
    }
    it("should require an mtype") {
      val tags = Map("unit" -> "P")
      val meta = Map[String, String]()
      an [IllegalArgumentException] should be thrownBy new MetricDefinition(tags.asJava, meta.asJava)
    }
  }
}
