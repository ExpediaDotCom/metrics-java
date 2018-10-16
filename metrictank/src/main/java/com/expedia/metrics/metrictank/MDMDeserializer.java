package com.expedia.metrics.metrictank;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Deserializer that reads data from Metrictank Kafka-mdm
 */
public class MDMDeserializer {
    private static final int METRIC_POINT_SIZE = 32;

    private final MessagePackSerializer messagePackSerializer;

    public MDMDeserializer() {
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
                return new MDMData(readMetricPoint(buffer));
            case 3:
                throw new IOException("MetricPointWithoutOrg is not supported");
            default:
                buffer.position(buffer.position()-1);
                return new MDMData(messagePackSerializer.deserialize(buffer));
        }
    }

    // To match format at https://github.com/raintank/schema/blob/88d6f01b3d265ddaa3c2ff110a0ed205145df57f/metricpoint.go#L56
    private MetricPoint readMetricPoint(ByteBuffer buffer) throws IOException {
        if (buffer.capacity() - buffer.position() != METRIC_POINT_SIZE) {
            throw new IOException("Insufficient bytes to hold a MetricPoint");
        }
        final byte[] id = new byte[16];
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.get(id);
        final double value = buffer.getDouble();
        final long time = Integer.toUnsignedLong(buffer.getInt());
        final int orgId = buffer.getInt();
        return new MetricPoint(new MetricKey(orgId, id), value, time);
    }
}
