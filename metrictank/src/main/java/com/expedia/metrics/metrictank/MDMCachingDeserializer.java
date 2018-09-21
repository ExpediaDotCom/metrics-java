package com.expedia.metrics.metrictank;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;

public class MDMCachingDeserializer {
    private static final Duration DEFAULT_V2_CLEAR_INTERVAL = Duration.ofHours(1);

    private final Cache<MetricKey, MetricDefinition> cache;
    private final MDMDeserializer deserializer;
    private final MetricTankIdFactory idFactory;

    /**
     * Constructs an MDMCachingDeserializer with a default cache
     */
    public MDMCachingDeserializer() {
        this(CacheBuilder.newBuilder()
                .expireAfterAccess(DEFAULT_V2_CLEAR_INTERVAL.plusMinutes(1))
                .build());
    }

    /**
     * Constructs an MDMCachingDeserializer that uses the supplied cache. It is recommended that the cache use an
     * expireAfterAccess duration that is slightly greater than the tsdb-gw v2-clear-interval, e.g. see
     * https://github.com/raintank/tsdb-gw/blob/a6a58c47a16723a36b9ac96b462cf61f79f1e205/scripts/config/tsdb-gw.ini#L38
     */
    public MDMCachingDeserializer(Cache<MetricKey, MetricDefinition> cache) {
        this.cache = cache;
        deserializer = new MDMDeserializer();
        idFactory = new MetricTankIdFactory();
    }

    /**
     * @return a MetricData or null if the buffer holds data that can't be mapped to a MetricData
     * @throws IOException when the buffer can't be deserialized
     */
    public MetricData deserialize(ByteBuffer buffer) throws IOException {
        MDMData mdmData = deserializer.deserialize(buffer);
        if (mdmData.isMetricData()) {
            MetricData metricData = mdmData.getMetricData();
            cache.put(idFactory.getKey(metricData.getMetricDefinition()), metricData.getMetricDefinition());
            return metricData;
        } else if (mdmData.isMetricPoint()) {
            MetricPoint metricPoint = mdmData.getMetricPoint();
            MetricDefinition metricDefinition = cache.getIfPresent(metricPoint.getKey());
            if (metricDefinition == null) {
                return null;
            }
            return new MetricData(metricDefinition, metricPoint.getValue(), metricPoint.getTime());
        }

        throw new IOException("Unknown MDMData type: "+mdmData);
    }
}
