/*
 * Copyright 2019 Expedia Group, Inc.
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

import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;

import java.util.Objects;

public class MetricTankMetricDefinition extends MetricDefinition {
    private final int orgId;
    private final int interval;
    private final String unit;
    private final String mtype;

    public MetricTankMetricDefinition(String key, int orgId, int interval, String unit, String mtype) {
        this(key, TagCollection.EMPTY, TagCollection.EMPTY, orgId, interval, unit, mtype);
    }

    public MetricTankMetricDefinition(String key, TagCollection tags, TagCollection meta, int orgId, int interval, String unit, String mtype) {
        super(key, tags, meta);
        this.orgId = orgId;
        this.interval = interval;
        if (unit == null) {
            throw new IllegalArgumentException("Unit may not be null");
        }
        this.unit = unit;
        if (mtype == null) {
            throw new IllegalArgumentException("Mtype may not be null");
        }
        this.mtype = mtype;
    }

    public int getOrgId() {
        return orgId;
    }

    public int getInterval() {
        return interval;
    }

    public String getUnit() {
        return unit;
    }

    public String getMtype() {
        return mtype;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetricTankMetricDefinition)) return false;
        if (!super.equals(o)) return false;
        MetricTankMetricDefinition that = (MetricTankMetricDefinition) o;
        return orgId == that.orgId &&
                interval == that.interval &&
                unit.equals(that.unit) &&
                mtype.equals(that.mtype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), orgId, interval, unit, mtype);
    }

    @Override
    public String toString() {
        return "MetricTankMetricDefinition{" +
                "orgId=" + orgId +
                ", interval=" + interval +
                ", unit='" + unit + '\'' +
                ", mtype='" + mtype + '\'' +
                ", key='" + key + '\'' +
                ", tags=" + tags +
                ", meta=" + meta +
                '}';
    }
}
