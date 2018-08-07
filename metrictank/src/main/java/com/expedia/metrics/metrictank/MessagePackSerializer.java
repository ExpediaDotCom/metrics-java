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
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class MessagePackSerializer implements MetricDataSerializer {
    public static final String ORG_ID = "org_id";
    public static final String NAME = "name";
    public static final String INTERVAL = "interval";

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
        if (!metric.metricDefinition.extrinsicTags.isEmpty()) {
            throw new IllegalArgumentException("Metrictank does not support extrinsic tags");
        }
        Map<String, String> tags = new HashMap<>(metric.metricDefinition.intrinsicTags);
        final Integer orgId = Integer.valueOf(tags.remove(ORG_ID));
        final String name = tags.remove(NAME);
        final Integer interval = Integer.valueOf(tags.remove(INTERVAL));
        final String unit = tags.remove(MetricDefinition.UNIT);
        final String mtype = tags.remove(MetricDefinition.MTYPE);
        List<String> formattedTags = formatTags(tags);
        final String id = computeId(orgId, name, unit, mtype, interval, formattedTags);

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
        List<String> tags = new ArrayList<>(numTags);
        for (int i=0; i < numTags; i++) {
            tags.add(unpacker.unpackString());
        }

        final Map<String, String> intrinsicTags = new HashMap<>();
        final Map<String, String> extrinsicTags = Collections.emptyMap();

        intrinsicTags.put(ORG_ID, Integer.toString(orgId));
        intrinsicTags.put(NAME, name);
        intrinsicTags.put(INTERVAL, Integer.toString(interval));
        intrinsicTags.put(MetricDefinition.UNIT, unit);
        intrinsicTags.put(MetricDefinition.MTYPE, mtype);
        for (final String tag : tags) {
            final int pos = tag.indexOf('=');
            if (pos == -1) {
                throw new IOException("Read a tag with no '=': "+tag);
            }
            final String tagKey = tag.substring(0, pos);
            final String tagValue = tag.substring(pos+1);
            intrinsicTags.put(tagKey, tagValue);
        }

        return new MetricData(new MetricDefinition(intrinsicTags, extrinsicTags), value, timestamp);
    }

    private void unpackString(String expected, MessageUnpacker unpacker) throws IOException {
        final String actual = unpacker.unpackString();
        if (!actual.equals(expected)) {
            throw new IOException("Expected field "+expected+" but got "+actual);
        }
    }

    private String computeId(Integer orgId, String name, String unit, String mtype, Integer interval, List<String> tags) {

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("The JRE is missing required digest algorithm MD5", e);
        }
        md.update(name.getBytes(StandardCharsets.UTF_8));
        md.update((byte)0);
        md.update(unit.getBytes(StandardCharsets.UTF_8));
        md.update((byte)0);
        md.update(mtype.getBytes(StandardCharsets.UTF_8));
        md.update((byte)0);
        md.update(interval.toString().getBytes(StandardCharsets.UTF_8));
        for (final String tag : tags) {
            md.update((byte)0);
            md.update(tag.getBytes(StandardCharsets.UTF_8));
        }
        final StringBuilder builder = new StringBuilder()
                .append(orgId)
                .append('.')
                .append(Hex.encodeHex(md.digest()));
        return builder.toString();
    }

    private List<String> formatTags(Map<String, String> tags) {
        List<String> result = new ArrayList<>(tags.size());

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            if (StringUtils.isEmpty(key) || key.contains("=") || key.contains(";") || key.contains("!")) {
                throw new IllegalArgumentException("Metric tank does not support key: " + key);
            }
            if (StringUtils.isEmpty(value) || value.contains(";")) {
                throw new IllegalArgumentException("Metric tank does not support value [" + value + "] for key " + key);
            }
            result.add(key + "=" + value);
        }

        Collections.sort(result);
        return result;
    }
}
