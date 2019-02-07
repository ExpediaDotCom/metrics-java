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
