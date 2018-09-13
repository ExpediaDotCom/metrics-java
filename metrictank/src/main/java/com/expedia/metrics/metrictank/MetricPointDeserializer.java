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
import java.nio.ByteOrder;
import java.util.Arrays;

public class MetricPointDeserializer {
    private static final int V2_TYPE_SIZE = 33;
    private static final int MP_INDICATOR = 2;


    /**
     * Deserialize bytes to MetricPoint.
     * Reference - https://github.com/raintank/schema/blob/master/metricpoint.go
     * @param data
     * @return
     */
    public MetricPoint deserialize(byte[] data) throws IOException {

        if (data.length != V2_TYPE_SIZE || data[0] != MP_INDICATOR) {
            throw new IOException("Not a MetricPoint format");
        }

        return deserializeV2(Arrays.copyOfRange(data, 1, data.length));
    }

    private MetricPoint deserializeV2(byte[] data) {
        final byte[] id = new byte[16];
        final ByteBuffer buffer = ByteBuffer.wrap(data)
            .order(ByteOrder.LITTLE_ENDIAN);
        buffer.get(id);
        final double value = buffer.getDouble();
        final long time = buffer.getInt();
        final int orgId = buffer.getInt();
        return new MetricPoint(new MetricKey(orgId, id), value, time);
    }
}
