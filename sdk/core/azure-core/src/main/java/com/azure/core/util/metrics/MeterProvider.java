package com.azure.core.util.metrics;

import com.azure.core.util.MetricsOptions;

public interface MeterProvider<T> {
    DoubleHistogram getDoubleHistogram(String metricName, String metricDescription, String unit, MetricsOptions<T> options);
}
