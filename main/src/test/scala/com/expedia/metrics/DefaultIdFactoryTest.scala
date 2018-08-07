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

import org.scalatest.{FunSpec, Matchers}

import scala.collection.JavaConverters._

class DefaultIdFactoryTest extends FunSpec with Matchers {

  describe("DefaultIdFactoryTest") {
    val idFactory = new DefaultIdFactory()
    val intrinsicTags = Map(
      MetricDefinition.UNIT -> "P",
      MetricDefinition.MTYPE -> "gauge",
      "intrinsictag" -> "intrinsicvalue"
    )
    val extrinsicTags = Map[String, String](
      "extrinsictag" -> "extrinsicvalue"
    )
    val metric = new MetricDefinition(intrinsicTags.asJava, extrinsicTags.asJava)


    it("getId should sort intrinsic tags and ignore extrinsic tags") {
      val id = idFactory.getId(metric)
      id should be("intrinsictag=intrinsicvalue,mtype=gauge,unit=P")
    }

  }
}
