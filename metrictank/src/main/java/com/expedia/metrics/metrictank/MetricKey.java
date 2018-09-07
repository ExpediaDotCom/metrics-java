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

import java.util.Arrays;
import java.util.Objects;

public class MetricKey {
    private final int orgId;
    private final byte[] id;

    public MetricKey(int orgId, byte[] id) {
        this.orgId = orgId;
        this.id = id;
    }

    public int getOrgId() {
        return orgId;
    }

    public byte[] getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricKey metricKey = (MetricKey) o;
        return orgId == metricKey.orgId &&
            Arrays.equals(id, metricKey.id);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(orgId);
        result = 31 * result + Arrays.hashCode(id);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MetricKey{");
        sb.append("orgId=").append(orgId);
        sb.append(", id=").append(Arrays.toString(id));
        sb.append('}');
        return sb.toString();
    }
}
