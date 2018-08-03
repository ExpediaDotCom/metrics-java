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
        this.tags = Collections.unmodifiableList(new ArrayList<>(tags));

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
        id = builder.toString();
    }
}
