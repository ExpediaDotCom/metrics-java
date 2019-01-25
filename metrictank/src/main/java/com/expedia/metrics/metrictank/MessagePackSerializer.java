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
import com.google.common.collect.Maps;
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
    // Defaults chosen to match MetricTank Prometheus input
    // https://github.com/grafana/metrictank/blob/b1c2c1a877d08b75d28f150b9b9b68ec90d7db73/input/prometheus/prometheus.go#L105
    private static final int DEFAULT_ORG_ID = 1;
    private static final int DEFAULT_INTERVAL = 15;
    private static final String DEFAULT_UNIT = "unknown";
    private static final String DEFAULT_MTYPE = "gauge";
    
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
        MetricDefinition md = metric.getMetricDefinition();
        serialize(metric, packer, getOrgId(md), getInterval(md), getUnit(md), getMtype(md));
    }

    static int getOrgId(MetricDefinition metric) {
        if (metric instanceof MetricTankMetricDefinition) {
            return ((MetricTankMetricDefinition) metric).getOrgId();
        }
        return DEFAULT_ORG_ID;
    }

    static int getInterval(MetricDefinition metric) {
        if (metric instanceof MetricTankMetricDefinition) {
            return ((MetricTankMetricDefinition) metric).getInterval();
        }
        return DEFAULT_INTERVAL;
    }

    static String getUnit(MetricDefinition metric) {
        if (metric instanceof MetricTankMetricDefinition) {
            return ((MetricTankMetricDefinition) metric).getUnit();
        }
        return DEFAULT_UNIT;
    }

    static String getMtype(MetricDefinition metric) {
        if (metric instanceof MetricTankMetricDefinition) {
            return ((MetricTankMetricDefinition) metric).getMtype();
        }
        return DEFAULT_MTYPE;
    }

    private void serialize(MetricData metric, MessagePacker packer, int orgId, int interval, String unit, String mtype) throws IOException {
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
        if (unit == null) {
            throw new IOException("Tag 'unit' is required by metrictank");
        }
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
        int orgId = 0;
        String name = "";
        int interval = 0;
        double value = 0.0;
        String unit = "";
        long timestamp = 0L;
        String mtype = "";
        List<String> rawTags = Collections.emptyList();
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
        
        throwIfMissing("OrgId", orgId == 0);
        throwIfMissing("Name", name.isEmpty());
        throwIfMissing("Interval", interval == 0);
        throwIfMissing("Mtype", mtype.isEmpty());
        
        final Map<String, String> kvTags = Maps.newHashMapWithExpectedSize(rawTags.size());
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
        return new MetricData(new MetricTankMetricDefinition(name, tags, TagCollection.EMPTY, orgId, interval, unit, mtype), value, timestamp);
    }
    
    private void throwIfMissing(String fieldName, boolean isMissing) throws IOException {
        if (isMissing) {
            throw new IOException("Missing required field: "+fieldName);
        }
    }
}
