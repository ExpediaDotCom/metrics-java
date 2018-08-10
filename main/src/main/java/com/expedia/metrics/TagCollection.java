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

public class TagCollection {
    public static final TagCollection EMPTY = new TagCollection(Collections.emptyMap(), Collections.emptySet());

    public final Map<String, String> kv;
    public final Set<String> v;

    public TagCollection(Map<String, String> kv) {
        this(kv, Collections.emptySet());
    }

    public TagCollection(Map<String, String> kv, Set<String> v) {
        this.kv = Collections.unmodifiableMap(new HashMap<>(kv));
        this.v = Collections.unmodifiableSet(new HashSet<>(v));
    }

    public boolean isEmpty() {
        return kv.isEmpty() && v.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagCollection that = (TagCollection) o;
        return Objects.equals(kv, that.kv) &&
                Objects.equals(v, that.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kv, v);
    }
}
