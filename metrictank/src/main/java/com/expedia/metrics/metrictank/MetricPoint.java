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

import java.util.Objects;

public class MetricPoint {

    private final MetricKey key;
    private final double value;
    private final long time;

    public MetricPoint(MetricKey key, double value, long time) {
        this.key = key;
        this.value = value;
        this.time = time;
    }

    public MetricKey getKey() {
        return key;
    }

    public double getValue() {
        return value;
    }

    public long getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricPoint that = (MetricPoint) o;
        return Double.compare(that.value, value) == 0 &&
            time == that.time &&
            Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, time);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MetricPoint{");
        sb.append("key=").append(key);
        sb.append(", value=").append(value);
        sb.append(", time=").append(time);
        sb.append('}');
        return sb.toString();
    }
}
