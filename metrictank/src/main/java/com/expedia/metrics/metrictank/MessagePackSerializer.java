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

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDataSerializer;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class MessagePackSerializer implements MetricDataSerializer {
    public static final String ORG_ID = "org_id";
    public static final String NAME = "name";
    public static final String INTERVAL = "interval";

    private static final int METRIC_NUM_FIELDS = 9;
    private static final int INT32_BYTES = 5;

    private final MetricTankIdFactory idFactory = new MetricTankIdFactory();

    @Override
    public byte[] serialize(MetricData metric) throws IOException {
        final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        serialize(metric, packer);
        packer.close();
        return packer.toByteArray();
    }

    @Override
    public byte[] serializeList(List<MetricData> metrics) throws IOException {
        final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packArrayHeader(metrics.size());
        for (final MetricData metric : metrics) {
            serialize(metric, packer);
        }
        packer.close();
        return packer.toByteArray();
    }

    @Override
    public MetricData deserialize(byte[] bytes) throws IOException {
        final MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
        MetricData metricData = deserialize(unpacker);
        unpacker.close();
        return metricData;
    }

    @Override
    public List<MetricData> deserializeList(byte[] bytes) throws IOException {
        final MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
        final int numMetrics = unpacker.unpackArrayHeader();
        List<MetricData> metrics = new ArrayList<>(numMetrics);
        for (int i=0; i < numMetrics; i++) {
            metrics.add(deserialize(unpacker));
        }
        unpacker.close();
        return metrics;
    }

    private void serialize(MetricData metric, MessagePacker packer) throws IOException {
        if (!metric.metricDefinition.meta.isEmpty()) {
            throw new IOException("Metrictank does not support meta tags");
        }
        if (!metric.metricDefinition.tags.v.isEmpty()) {
            throw new IOException("Metrictank does not support value tags");
        }
        Map<String, String> tags = new HashMap<>(metric.metricDefinition.tags.kv);
        final int orgId;
        try {
            orgId = Integer.parseInt(tags.remove(ORG_ID));
        } catch (NumberFormatException e) {
            throw new IOException("Org ID must be an int", e);
        }
        final String name = tags.remove(NAME);
        if (name == null) {
            throw new IOException("Name tag is required by metrictank");
        }
        final int interval;
        try {
            interval = Integer.parseInt(tags.remove(INTERVAL));
        } catch (NumberFormatException e) {
            throw new IOException("Interval must be an int", e);
        }
        final String unit = tags.remove(MetricDefinition.UNIT);
        if (unit == null) {
            throw new IOException("Unit tag is required by metrictank");
        }
        final String mtype = tags.remove(MetricDefinition.MTYPE);
        if (mtype == null) {
            throw new IOException("Mtype tag is required by metrictank");
        }
        List<String> formattedTags = idFactory.formatTags(tags);
        final String id = idFactory.getId(orgId, name, unit, mtype, interval, formattedTags);

        packer.packMapHeader(METRIC_NUM_FIELDS);
        packer.packString("id");
        packer.packString(id);
        packer.packString("org_id");
        packer.packInt(orgId);
        packer.packString("name");
        packer.packString(name);
        packer.packString("interval");
        packer.packInt(interval);
        packer.packString("value");
        packer.packDouble(metric.value);
        packer.packString("unit");
        packer.packString(unit);
        packer.packString("time");

        // packLong auto converts to narrowest int type, but Raintank requires a signed int, so we manually pack the time
        final ByteBuffer b = ByteBuffer.allocate(INT32_BYTES);
        b.put(MessagePack.Code.INT32);
        b.putInt(metric.timestamp.intValue());
        packer.addPayload(b.array());

        packer.packString("mtype");
        packer.packString(mtype);
        packer.packString("tags");
        packer.packArrayHeader(formattedTags.size());
        for (final String tag : formattedTags) {
            packer.packString(tag);
        }
    }

    private MetricData deserialize(MessageUnpacker unpacker) throws IOException {
        int numFields = unpacker.unpackMapHeader();
        if (numFields != METRIC_NUM_FIELDS) {
            throw new IOException("Metric should have "+METRIC_NUM_FIELDS+" fields, but has "+numFields);
        }
        unpackString("id", unpacker);
        final String id = unpacker.unpackString();
        unpackString("org_id", unpacker);
        final int orgId = unpacker.unpackInt();
        unpackString("name", unpacker);
        final String name = unpacker.unpackString();
        unpackString("interval", unpacker);
        final int interval = unpacker.unpackInt();
        unpackString("value", unpacker);
        final double value = unpacker.unpackDouble();
        unpackString("unit", unpacker);
        final String unit = unpacker.unpackString();
        unpackString("time", unpacker);
        final long timestamp = unpacker.unpackLong();
        unpackString("mtype", unpacker);
        final String mtype = unpacker.unpackString();
        unpackString("tags", unpacker);
        final int numTags = unpacker.unpackArrayHeader();
        List<String> rawTags = new ArrayList<>(numTags);
        for (int i=0; i < numTags; i++) {
            rawTags.add(unpacker.unpackString());
        }

        final Map<String, String> kvTags = new HashMap<>();

        kvTags.put(ORG_ID, Integer.toString(orgId));
        kvTags.put(NAME, name);
        kvTags.put(INTERVAL, Integer.toString(interval));
        kvTags.put(MetricDefinition.UNIT, unit);
        kvTags.put(MetricDefinition.MTYPE, mtype);
        for (final String tag : rawTags) {
            final int pos = tag.indexOf('=');
            if (pos == -1) {
                throw new IOException("Read a tag with no '=': "+tag);
            }
            final String tagKey = tag.substring(0, pos);
            final String tagValue = tag.substring(pos+1);
            kvTags.put(tagKey, tagValue);
        }

        TagCollection tags = new TagCollection(kvTags);
        return new MetricData(new MetricDefinition(tags, TagCollection.EMPTY), value, timestamp);
    }

    private void unpackString(String expected, MessageUnpacker unpacker) throws IOException {
        final String actual = unpacker.unpackString();
        if (!actual.equals(expected)) {
            throw new IOException("Expected field "+expected+" but got "+actual);
        }
    }
}
