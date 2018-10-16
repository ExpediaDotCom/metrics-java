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
