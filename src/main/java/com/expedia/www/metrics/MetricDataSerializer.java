package com.expedia.www.metrics;

import java.io.IOException;
import java.util.List;

public interface MetricDataSerializer {
    public byte[] serialize(MetricData metric) throws IOException;
    public byte[] serialize(List<MetricData> metrics) throws IOException;
}
