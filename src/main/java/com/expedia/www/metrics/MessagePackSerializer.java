package com.expedia.www.metrics;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class MessagePackSerializer implements MetricDataSerializer {
    private static final int METRIC_NUM_FIELDS = 9;
    private static final int INT32_BYTES = 5;

    @Override
    public byte[] serialize(MetricData metric) throws IOException {
        final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        serialize(metric, packer);
        packer.close();
        return packer.toByteArray();
    }

    @Override
    public byte[] serialize(List<MetricData> metrics) throws IOException {
        final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        serialize(metrics, packer);
        packer.close();
        return packer.toByteArray();
    }

    private void serialize(MetricData metric, MessagePacker packer) throws IOException {
        packer.packMapHeader(METRIC_NUM_FIELDS);
        packer.packString("id");
        packer.packString(metric.id);
        packer.packString("org_id");
        packer.packInt(metric.orgId);
        packer.packString("name");
        packer.packString(metric.name);
        packer.packString("interval");
        packer.packInt(metric.interval);
        packer.packString("value");
        packer.packDouble(metric.value);
        packer.packString("unit");
        packer.packString(metric.unit);
        packer.packString("time");

        // packLong auto converts to narrowest int type, but Raintank requires a signed int, so we manually pack the time
        final ByteBuffer b = ByteBuffer.allocate(INT32_BYTES);
        b.put(MessagePack.Code.INT32);
        b.putInt(metric.time.intValue());
        packer.addPayload(b.array());

        packer.packString("mtype");
        packer.packString(metric.mtype);
        packer.packString("tags");
        packer.packArrayHeader(metric.tags.size());
        for (final String tag : metric.tags) {
            packer.packString(tag);
        }
    }

    private void serialize(List<MetricData> metrics, MessagePacker packer) throws IOException {
        packer.packArrayHeader(metrics.size());
        for (final MetricData metric : metrics) {
            serialize(metric, packer);
        }
    }
}
