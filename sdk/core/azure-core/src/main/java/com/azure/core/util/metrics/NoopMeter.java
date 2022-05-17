package com.azure.core.util.metrics;

import java.util.Map;

class NoopMeter implements AzureMeter {

    private static final AzureLongHistogram NOOP_LONG_HISTOGRAM = (value, context) -> {
    };
    private static final AzureLongCounter NOOP_LONG_COUNTER = (value, context) -> {
    };

    NoopMeter() {
    }

    @Override
    public AzureLongHistogram createLongHistogram(String name, String metricDescription, String unit, Map<String, Object> attributes) {
        return NOOP_LONG_HISTOGRAM;
    }

    @Override
    public AzureLongCounter createLongCounter(String name, String metricDescription, String unit, Map<String, Object> attributes) {
        return NOOP_LONG_COUNTER;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
