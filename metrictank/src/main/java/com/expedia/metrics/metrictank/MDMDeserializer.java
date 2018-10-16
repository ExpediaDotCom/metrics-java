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
package com.expedia.metrics.metrictank;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Deserializer that reads data from Metrictank Kafka-mdm
 */
public class MDMDeserializer {
    private final MetricPointSerializer metricPointSerializer;
    private final MessagePackSerializer messagePackSerializer;

    public MDMDeserializer() {
        metricPointSerializer = new MetricPointSerializer();
        messagePackSerializer = new MessagePackSerializer();
    }

    public MDMData deserialize(ByteBuffer buffer) throws IOException {
        byte format = buffer.get();
        switch (format) {
            case 0:
                throw new IOException("MetricDataArrayJson is not supported");
            case 1:
                throw new IOException("MetricDataArrayMsgp is not supported");
            case 2:
                return new MDMData(metricPointSerializer.deserialize(buffer));
            case 3:
                throw new IOException("MetricPointWithoutOrg is not supported");
            default:
                buffer.position(buffer.position()-1);
                return new MDMData(messagePackSerializer.deserialize(buffer));
        }
    }
}
