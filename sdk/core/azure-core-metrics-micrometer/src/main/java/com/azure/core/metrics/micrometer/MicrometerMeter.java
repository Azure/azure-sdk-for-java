package com.azure.core.metrics.micrometer;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.ClientLongCounter;
import com.azure.core.util.metrics.ClientMeter;
import com.azure.core.util.metrics.ClientLongHistogram;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

import java.util.Map;

public class MicrometerMeter implements ClientMeter {
    private final MeterRegistry registry;

    public MicrometerMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        Object registryImpl = options.getImplementationConfiguration();
        if (registryImpl != null && MeterRegistry.class.isAssignableFrom(registryImpl.getClass())) {
            registry = (MeterRegistry) registryImpl;
        } else {
            registry = Metrics.globalRegistry;
        }
    }

    @Override
    public ClientLongHistogram getLongHistogram(String metricName, String metricDescription, String unit, Map<String, Object> attributes) {
        DistributionSummary.Builder summaryBuilder = DistributionSummary.builder(metricName);
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, Object> tag : attributes.entrySet()) {
                summaryBuilder.tag(tag.getKey(), tag.getValue().toString());
            }
        }

        // todo vs Cumulative
        DistributionSummary summary = summaryBuilder
            .baseUnit(unit)
            .description(metricDescription)
            .publishPercentileHistogram(true)
            .register(registry);

        return new MicrometerLongHistogram(summary);
    }

    @Override
    public ClientLongCounter getLongCounter(String metricName, String metricDescription, String unit, Map<String, Object> attributes) {
        Counter.Builder counterBuilder = Counter.builder(metricName);
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, Object> tag : attributes.entrySet()) {
                counterBuilder.tag(tag.getKey(), tag.getValue().toString());
            }
        }

        // todo vs Cumulative
        Counter counter = counterBuilder
            .baseUnit(unit)
            .description(metricDescription)
            .register(registry);

        return new MicrometerLongCounter(counter);
    }
}
