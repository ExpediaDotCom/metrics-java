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
package com.expedia.metrics;

import java.util.*;

public class MetricDefinition {
    public static final String UNIT = "unit";
    public static final String MTYPE = "mtype";

    private static final List<String> REQUIRED_TAGS = new ArrayList<>(2);
    static {
        REQUIRED_TAGS.add(UNIT);
        REQUIRED_TAGS.add(MTYPE);
    }

    public final Map<String, String> tags;
    public final Map<String, String> meta;

    public MetricDefinition(Map<String, String> tags, Map<String, String> meta) {
        for (String tag : REQUIRED_TAGS) {
            if (!tags.containsKey(tag)) {
                throw new IllegalArgumentException("Missing required tag: " + tag);
            }
        }
        this.tags = Collections.unmodifiableMap(new HashMap<>(tags));
        this.meta = Collections.unmodifiableMap(new HashMap<>(meta));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MetricDefinition that = (MetricDefinition) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
                .append(tags, that.tags)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
                .append(tags)
                .toHashCode();
    }
}
