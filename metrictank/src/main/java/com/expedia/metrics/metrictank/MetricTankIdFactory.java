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

import com.expedia.metrics.IdFactory;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.util.Encoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.expedia.metrics.metrictank.MessagePackSerializer.INTERVAL;
import static com.expedia.metrics.metrictank.MessagePackSerializer.ORG_ID;

public class MetricTankIdFactory implements IdFactory {
    
    @Override
    public String getId(MetricDefinition metric) {
        Map<String, String> tags = new HashMap<>(metric.getTags().getKv());
        final int orgId;
        try {
            orgId = Integer.parseInt(tags.remove(ORG_ID));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tag 'org_id' must be an int", e);
        }
        final String name = metric.getKey();
        if (name == null) {
            throw new IllegalArgumentException("Tag 'key' is required by metrictank");
        }
        final int interval;
        try {
            interval = Integer.parseInt(tags.remove(INTERVAL));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tag 'interval' must be an int", e);
        }
        final String unit = tags.remove(MetricDefinition.UNIT);
        if (unit == null) {
            throw new IllegalArgumentException("Tag 'unit' is required by metrictank");
        }
        final String mtype = tags.remove(MetricDefinition.MTYPE);
        if (mtype == null) {
            throw new IllegalArgumentException("Tag 'mtype' is required by metrictank");
        }
        List<String> formattedTags = formatTags(tags);
        return getId(orgId, name, unit, mtype, interval, formattedTags);
    }

    public String getId(int orgId, String name, String unit, String mtype, int interval, List<String> tags) {
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
        md.update(Integer.toString(interval).getBytes(StandardCharsets.UTF_8));
        for (final String tag : tags) {
            md.update((byte)0);
            md.update(tag.getBytes(StandardCharsets.UTF_8));
        }
        String orgIdStr = Integer.toString(orgId);
        byte[] md5sum = md.digest();
        final StringBuilder builder = new StringBuilder(orgIdStr.length() + 1 + md5sum.length*2)
                .append(orgId)
                .append('.');
        Encoder.encodeHex(builder, md5sum);
        return builder.toString();
    }

    public List<String> formatTags(Map<String, String> tags) {
        List<String> result = new ArrayList<>(tags.size());

        for (Map.Entry<String, String> entry : tags.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            if (key == null || key.isEmpty() || key.contains("=") || key.contains(";") || key.contains("!")) {
                throw new IllegalArgumentException("Metric tank does not support key: " + key);
            }
            if (value == null || value.isEmpty() || value.contains(";")) {
                throw new IllegalArgumentException("Metric tank does not support value [" + value + "] for key " + key);
            }
            result.add(key + "=" + value);
        }

        Collections.sort(result);
        return result;
    }
}
