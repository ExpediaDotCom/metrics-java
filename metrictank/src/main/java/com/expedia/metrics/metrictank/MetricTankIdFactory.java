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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.expedia.metrics.metrictank.MessagePackSerializer.INTERVAL;
import static com.expedia.metrics.metrictank.MessagePackSerializer.NAME;
import static com.expedia.metrics.metrictank.MessagePackSerializer.ORG_ID;

public class MetricTankIdFactory implements IdFactory {
    @Override
    public String getId(MetricDefinition metric) {
        Map<String, String> tags = new HashMap<>(metric.intrinsicTags);
        final Integer orgId = Integer.valueOf(tags.remove(ORG_ID));
        final String name = tags.remove(NAME);
        final Integer interval = Integer.valueOf(tags.remove(INTERVAL));
        final String unit = tags.remove(MetricDefinition.UNIT);
        final String mtype = tags.remove(MetricDefinition.MTYPE);
        List<String> formattedTags = MessagePackSerializer.formatTags(tags);
        return getId(orgId, name, unit, mtype, interval, formattedTags);
    }

    String getId(Integer orgId, String name, String unit, String mtype, Integer interval, List<String> tags) {
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
}
