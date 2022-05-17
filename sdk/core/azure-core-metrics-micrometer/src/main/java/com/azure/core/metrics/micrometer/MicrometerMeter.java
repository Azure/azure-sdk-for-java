package com.azure.core.metrics.micrometer;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.AzureLongCounter;
import com.azure.core.util.metrics.AzureMeter;
import com.azure.core.util.metrics.AzureLongHistogram;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

import java.util.Map;

public class MicrometerMeter implements AzureMeter {
    private final MeterRegistry registry;

    public MicrometerMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        if (MeterRegistry.class.isAssignableFrom(options.getProvider().getClass())) {
            registry = (MeterRegistry) options.getProvider();
        } else {
            registry = Metrics.globalRegistry;
        }
    }

    @Override
    public AzureLongHistogram createLongHistogram(String name, String description, String unit, Map<String, Object> attributes) {
        DistributionSummary.Builder summaryBuilder = DistributionSummary.builder(name);
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, Object> tag : attributes.entrySet()) {
                summaryBuilder.tag(tag.getKey(), tag.getValue().toString());
            }
        }

        if (!CoreUtils.isNullOrEmpty(unit)) {
            summaryBuilder.baseUnit(unit);
        }

        // todo vs Cumulative
        DistributionSummary summary = summaryBuilder
            .description(description)
            .publishPercentileHistogram(true)
            .register(registry);

        return new MicrometerLongHistogram(summary);
    }

    @Override
    public AzureLongCounter createLongCounter(String name, String description, String unit, Map<String, Object> attributes) {
        Counter.Builder counterBuilder = Counter.builder(name);
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, Object> tag : attributes.entrySet()) {
                counterBuilder.tag(tag.getKey(), tag.getValue().toString());
            }
        }

        if (!CoreUtils.isNullOrEmpty(unit)) {
            counterBuilder.baseUnit(unit);
        }

        // todo vs Cumulative
        Counter counter = counterBuilder
            .description(description)
            .register(registry);

        return new MicrometerLongCounter(counter);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
