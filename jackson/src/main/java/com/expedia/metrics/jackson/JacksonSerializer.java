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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class JacksonSerializer implements MetricDataSerializer {
    private final ObjectMapper mapper;

    public JacksonSerializer() {
        this(new ObjectMapper());
    }

    public JacksonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
        mapper.registerModule(new MetricsJavaModule());
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
}
