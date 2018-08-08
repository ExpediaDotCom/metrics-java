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
package com.expedia.metrics.jackson;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDataSerializer;
import com.expedia.metrics.MetricDefinition;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JacksonSerializer implements MetricDataSerializer {
    final ObjectMapper mapper;

    public JacksonSerializer() {
        mapper = new ObjectMapper();
        mapper.addMixIn(MetricData.class, MetricDataMixin.class);
        mapper.addMixIn(MetricDefinition.class, MetricDefinitionMixin.class);
    }

    @Override
    public byte[] serialize(MetricData metric) throws IOException {
        return mapper.writeValueAsBytes(metric);
    }

    @Override
    public byte[] serializeList(List<MetricData> metrics) throws IOException {
        return mapper.writeValueAsBytes(metrics);
    }

    @Override
    public MetricData deserialize(byte[] bytes) throws IOException {
        return mapper.readValue(bytes, MetricData.class);
    }

    @Override
    public List<MetricData> deserializeList(byte[] bytes) throws IOException {
        return mapper.readValue(bytes, new TypeReference<List<MetricData>>() {});
    }

    private static class MetricDataMixin {
        @JsonCreator
        MetricDataMixin(
                @JsonProperty("metricDefinition") MetricDefinition metricDefinition,
                @JsonProperty("value") Double value,
                @JsonProperty("timestamp") Long timestamp) {}
    }

    private static class MetricDefinitionMixin {
        @JsonCreator
        MetricDefinitionMixin(
                @JsonProperty("tags") Map<String, String> tags,
                @JsonProperty("meta") Map<String, String> meta) {}
    }
}
