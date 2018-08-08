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
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.expedia.metrics.metrictank.MessagePackSerializer.INTERVAL;
import static com.expedia.metrics.metrictank.MessagePackSerializer.NAME;
import static com.expedia.metrics.metrictank.MessagePackSerializer.ORG_ID;

public class MetricTankIdFactory implements IdFactory {
    @Override
    public String getId(MetricDefinition metric) {
        Map<String, String> tags = new HashMap<>(metric.tags);
        final int orgId;
        try {
            orgId = Integer.parseInt(tags.remove(ORG_ID));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Org ID must be an int", e);
        }
        final String name = tags.remove(NAME);
        if (name == null) {
            throw new IllegalArgumentException("Name tag is required by metrictank");
        }
        final int interval;
        try {
            interval = Integer.parseInt(tags.remove(INTERVAL));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Interval must be an int", e);
        }
        final String unit = tags.remove(MetricDefinition.UNIT);
        if (unit == null) {
            throw new IllegalArgumentException("Unit tag is required by metrictank");
        }
        final String mtype = tags.remove(MetricDefinition.MTYPE);
        if (mtype == null) {
            throw new IllegalArgumentException("Mtype tag is required by metrictank");
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
        final StringBuilder builder = new StringBuilder()
                .append(orgId)
                .append('.')
                .append(Hex.encodeHex(md.digest()));
        return builder.toString();
    }

    List<String> formatTags(Map<String, String> tags) {
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
