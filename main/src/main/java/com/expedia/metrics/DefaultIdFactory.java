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

import java.util.Map;
import java.util.TreeMap;

public class DefaultIdFactory implements IdFactory {
    @Override
    public String getId(MetricDefinition metric) {
        final Map<String, String> sortedKvTags = new TreeMap<>(metric.tags.kv);
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedKvTags.entrySet()) {
            builder.append(entry.getKey());
            if (entry.getValue() != null) {
                    builder.append('=')
                        .append(entry.getValue());
            }
            builder.append(',');
        }
        for (String tag : metric.tags.v) {
            builder.append(tag)
                    .append(',');
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
