/*
 * Copyright 2019 Expedia Group, Inc.
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

import org.scalatest.{FunSpec, Matchers}

import scala.collection.JavaConverters._

class HashIdFactoryTest extends FunSpec with Matchers {

    describe("DefaultIdFactoryTest") {
        val idFactory = new HashIdFactory()
        val tags = new TagCollection(Map(
            MetricDefinition.UNIT -> "P",
            MetricDefinition.MTYPE -> "gauge",
            "tag" -> "value"
        ).asJava, Set("valuetag").asJava)
        val meta = new TagCollection(Map[String, String](
            "metatag" -> "metavalue"
        ).asJava)
        val metric = new MetricDefinition(tags, meta)

        it("getId should sort tags and ignore meta tags") {
            val id = idFactory.getId(metric)
            id should be("95b0a99e2de4591f2e3df916da63d80d")
        }
    }
}
