package com.expedia.metrics.metrictank;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePackException;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Deserializer that reads the internal Metrictank formats that can be read from kafka-mdm
 */
public class MDMDeserializer {
    private static final int METRIC_POINT_SIZE = 32;

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
                return new MDMData(readMetricData(buffer));
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

    // To match format at https://github.com/raintank/schema/blob/3660fadd2be921b1afce4d3d2ffda338c151150b/metric_gen.go#L12
    private MetricData readMetricData(ByteBuffer buffer) throws IOException {
        String name = null;
        String unit = "metric";
        double value = 0.0;
        long time = 0;
        final Map<String, String> kvTags = new HashMap<>();

        try {
            final MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffer);
            for (int fields = unpacker.unpackMapHeader(); fields > 0; fields--) {
                String fieldName = unpacker.unpackString();
                switch (fieldName) {
                    case "Id":
                        unpacker.unpackString();
                        break;
                    case "OrgId":
                        int orgId = unpacker.unpackInt();
                        kvTags.put(MessagePackSerializer.ORG_ID, Integer.toString(orgId));
                        break;
                    case "Name":
                        name = unpacker.unpackString();
                        break;
                    case "Interval":
                        long interval = unpacker.unpackLong();
                        kvTags.put(MessagePackSerializer.INTERVAL, Long.toString(interval));
                        break;
                    case "Value":
                        value = unpacker.unpackDouble();
                        break;
                    case "Unit":
                        unit = unpacker.unpackString();
                        break;
                    case "Time":
                        time = unpacker.unpackLong();
                        break;
                    case "Mtype":
                        String mtype = unpacker.unpackString();
                        kvTags.put(MetricDefinition.MTYPE, mtype);
                        break;
                    case "Tags":
                        int numTags = unpacker.unpackArrayHeader();
                        for (; numTags > 0; numTags--) {
                            String tag = unpacker.unpackString();
                            int pos = tag.indexOf('=');
                            if (pos == -1) {
                                throw new IOException("Read a tag with no '=': " + tag);
                            }
                            final String tagKey = tag.substring(0, pos);
                            final String tagValue = tag.substring(pos + 1);
                            kvTags.put(tagKey, tagValue);
                        }
                        break;
                    default:
                        unpacker.unpackValue();
                }
            }
            kvTags.put(MetricDefinition.UNIT, unit);
            return new MetricData(new MetricDefinition(name, new TagCollection(kvTags), TagCollection.EMPTY), value, time);
        } catch (MessagePackException e) {
            throw new IOException("Unable to deserialize MetricData", e);
        }
    }
}
