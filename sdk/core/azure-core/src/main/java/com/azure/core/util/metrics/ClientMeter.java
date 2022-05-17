package com.azure.core.util.metrics;

import java.util.Map;

public interface ClientMeter {
    ClientLongHistogram getLongHistogram(String metricName, String metricDescription, String unit, Map<String, Object> attributes);
    ClientLongCounter getLongCounter(String metricName, String metricDescription, String unit, Map<String, Object> attributes);
    boolean isEnabled();
}
