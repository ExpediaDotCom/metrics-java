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
import org.msgpack.core.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * This serializer reads and writes the Metrictank Kafka-mdm MetricData format
 *
 * @see <a href="https://github.com/grafana/metrictank/blob/master/docs/inputs.md#metricdata">MetricData</a>
 */
public class MessagePackSerializer implements MetricDataSerializer {
    public static final String ORG_ID = "org_id";
    public static final String INTERVAL = "interval";

    private static final int METRIC_NUM_FIELDS = 9;

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

    public MetricData deserialize(ByteBuffer buffer) throws IOException {
        try {
            final MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffer);
            MetricData metricData = deserialize(unpacker);
            unpacker.close();
            return metricData;
        } catch (MessagePackException e) {
            throw new IOException("Unable to deserialize MetricData", e);
        }
    }

    @Override
    public MetricData deserialize(byte[] bytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return deserialize(buffer);
    }

    public List<MetricData> deserializeList(ByteBuffer buffer) throws IOException {
        final MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffer);
        final int numMetrics = unpacker.unpackArrayHeader();
        List<MetricData> metrics = new ArrayList<>(numMetrics);
        for (int i=0; i < numMetrics; i++) {
            metrics.add(deserialize(unpacker));
        }
        unpacker.close();
        return metrics;
    }

    @Override
    public List<MetricData> deserializeList(byte[] bytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return deserializeList(buffer);
    }

    private void serialize(MetricData metric, MessagePacker packer) throws IOException {
        final String name = metric.getMetricDefinition().getKey();
        if (name == null) {
            throw new IOException("Key is required by metrictank");
        }
        if (!metric.getMetricDefinition().getMeta().isEmpty()) {
            throw new IOException("Metrictank does not support meta tags");
        }
        if (!metric.getMetricDefinition().getTags().getV().isEmpty()) {
            throw new IOException("Metrictank does not support value tags");
        }
        Map<String, String> tags = new HashMap<>(metric.getMetricDefinition().getTags().getKv());
        final int orgId;
        try {
            orgId = Integer.parseInt(tags.remove(ORG_ID));
        } catch (NumberFormatException e) {
            throw new IOException("Tag 'org_id' must be an int", e);
        }
        final int interval;
        try {
            interval = Integer.parseInt(tags.remove(INTERVAL));
        } catch (NumberFormatException e) {
            throw new IOException("Tag 'interval' must be an int", e);
        }
        final String unit = tags.remove(MetricDefinition.UNIT);
        if (unit == null) {
            throw new IOException("Tag 'unit' is required by metrictank");
        }
        final String mtype = tags.remove(MetricDefinition.MTYPE);
        if (mtype == null) {
            throw new IOException("Tag 'mtype' is required by metrictank");
        }
        List<String> formattedTags = idFactory.formatTags(tags);
        final String id = idFactory.getId(orgId, name, unit, mtype, interval, formattedTags);

        packer.packMapHeader(METRIC_NUM_FIELDS);
        packer.packString("Id");
        packer.packString(id);
        packer.packString("OrgId");
        packer.packInt(orgId);
        packer.packString("Name");
        packer.packString(name);
        packer.packString("Interval");
        packer.packInt(interval);
        packer.packString("Value");
        packer.packDouble(metric.getValue());
        packer.packString("Unit");
        packer.packString(unit);
        packer.packString("Time");

        // packLong auto converts to narrowest int type, but Raintank requires a signed int64, so we manually pack the time
        final ByteBuffer b = ByteBuffer.allocate(1 + Long.BYTES);
        b.put(MessagePack.Code.INT64);
        b.putLong(metric.getTimestamp());
        packer.addPayload(b.array());

        packer.packString("Mtype");
        packer.packString(mtype);
        packer.packString("Tags");
        packer.packArrayHeader(formattedTags.size());
        for (final String tag : formattedTags) {
            packer.packString(tag);
        }
    }

    private MetricData deserialize(MessageUnpacker unpacker) throws IOException {
        Integer orgId = null;
        String name = null;
        Integer interval = null;
        Double value = null;
        String unit = null;
        Long timestamp = null;
        String mtype = null;
        List<String> rawTags = null;
        for (int numFields = unpacker.unpackMapHeader(); numFields > 0; numFields--) {
            String fieldName = unpacker.unpackString();
            switch(fieldName) {
                case "Id":
                    unpacker.unpackString();
                    break;
                case "OrgId":
                    orgId = unpacker.unpackInt();
                    break;
                case "Name":
                name = unpacker.unpackString();
                    break;
                case "Interval":
                interval = unpacker.unpackInt();
                    break;
                case "Value":
                value = unpacker.unpackDouble();
                    break;
                case "Unit":
                unit = unpacker.unpackString();
                    break;
                case "Time":
                timestamp = unpacker.unpackLong();
                    break;
                case "Mtype":
                mtype = unpacker.unpackString();
                    break;
                case "Tags":
                    final int numTags = unpacker.unpackArrayHeader();
                    rawTags = new ArrayList<>(numTags);
                    for (int i = 0; i < numTags; i++) {
                        rawTags.add(unpacker.unpackString());
                    }
                    break;
                default:
                    // Discard unknown values
                    unpacker.unpackValue();
                    break;
            }
        }

        checkRequiredField("OrgId", orgId);
        checkRequiredField("Name", name);
        checkRequiredField("Interval", interval);
        checkRequiredField("Value", value);
        checkRequiredField("Unit", unit);
        checkRequiredField("Timestamp", timestamp);
        checkRequiredField("Mtype", mtype);
        checkRequiredField("Tags", rawTags);

        final Map<String, String> kvTags = new HashMap<>();

        kvTags.put(ORG_ID, orgId.toString());
        kvTags.put(INTERVAL, interval.toString());
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
        return new MetricData(new MetricDefinition(name, tags, TagCollection.EMPTY), value, timestamp);
    }

    private void checkRequiredField(String fieldName, Object value) throws IOException {
        if (value == null) {
            throw new IOException("Missing required field: "+fieldName);
        }
    }
}
