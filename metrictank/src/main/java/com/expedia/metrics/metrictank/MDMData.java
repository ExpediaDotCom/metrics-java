package com.expedia.metrics.metrictank;

import com.expedia.metrics.MetricData;

/**
 * Holder for a message read from kafak-mdm
 */
public class MDMData {
    private final MetricPoint metricPoint;
    private final MetricData metricData;

    MDMData(MetricPoint metricPoint) {
        this.metricPoint = metricPoint;
        this.metricData = null;
    }

    MDMData(MetricData metricData) {
        this.metricPoint = null;
        this.metricData = metricData;
    }

    public MetricPoint getMetricPoint() {
        return metricPoint;
    }

    public MetricData getMetricData() {
        return metricData;
    }

    public boolean isMetricPoint() {
        return metricPoint != null;
    }

    public boolean isMetricData() {
        return metricData != null;
    }
}
