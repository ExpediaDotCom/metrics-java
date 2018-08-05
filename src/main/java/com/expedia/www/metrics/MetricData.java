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
package com.expedia.www.metrics;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetricData {
    public final String id;
    public final Integer orgId;
    public final String name;
    public final Integer interval;
    public final Double value;
    public final String unit;
    public final Long time;
    public final String mtype;
    public final List<String> tags;

    public MetricData(Integer orgId, String name, Integer interval, Double value, String unit, Long time,
                      String mtype, List<String> tags) {
        this.orgId = orgId;
        this.name = name;
        this.interval = interval;
        this.value = value;
        this.unit = unit;
        this.time = time;
        this.mtype = mtype;
        ArrayList<String> list = new ArrayList<>(tags);
        Collections.sort(list);
        this.tags = Collections.unmodifiableList(list);

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
        for (final String tag : this.tags) {
            md.update((byte)0);
            md.update(tag.getBytes(StandardCharsets.UTF_8));
        }
        final StringBuilder builder = new StringBuilder()
                .append(orgId)
                .append('.')
                .append(Hex.encodeHex(md.digest()));
        id = builder.toString();
    }
}
