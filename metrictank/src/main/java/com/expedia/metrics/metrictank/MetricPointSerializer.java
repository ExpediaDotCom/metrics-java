package com.expedia.metrics.metrictank;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This serializer reads and writes the Metrictank Kafka-mdm MetricPoint format
 *
 * @see <a href="https://github.com/grafana/metrictank/blob/master/docs/inputs.md#metricpoint">MetricPoint</a>
 */
public class MetricPointSerializer {
    public static final int METRIC_POINT_BYTES = 32;

    public void serialize(MetricPoint metricPoint, ByteBuffer buffer) throws IOException {
        if (buffer.capacity() - buffer.position() < METRIC_POINT_BYTES) {
            throw new IOException("Insufficient capacity to hold a MetricPoint");
        }
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(metricPoint.getKey().getId());
        buffer.putDouble(metricPoint.getValue());
        buffer.putInt((int)metricPoint.getTime());
        buffer.putInt(metricPoint.getKey().getOrgId());
    }

    public byte[] serialize(MetricPoint metricPoint) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(MetricPointSerializer.METRIC_POINT_BYTES);
        serialize(metricPoint, buffer);
        return buffer.array();
    }

    public MetricPoint deserialize(ByteBuffer buffer) throws IOException {
        if (buffer.capacity() - buffer.position() < METRIC_POINT_BYTES) {
            throw new IOException("Insufficient capacity to hold a MetricPoint");
        }
        final byte[] id = new byte[16];
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.get(id);
        final double value = buffer.getDouble();
        final long time = Integer.toUnsignedLong(buffer.getInt());
        final int orgId = buffer.getInt();
        return new MetricPoint(new MetricKey(orgId, id), value, time);
    }

    public MetricPoint deserialize(byte[] bytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return deserialize(buffer);
    }
}
