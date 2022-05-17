package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.ClientLongCounter;
import com.azure.core.util.metrics.ClientLongHistogram;
import com.azure.core.util.metrics.ClientMeter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;

import java.util.Map;

public class OpenTelemetryMeter implements ClientMeter {
    private final static io.opentelemetry.api.metrics.MeterProvider DEFAULT_PROVIDER = GlobalOpenTelemetry.getMeterProvider();
    private final Meter meter;
    private final boolean isEnabled;

    public OpenTelemetryMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        Object providerImpl = options.getImplementationConfiguration();
        MeterProvider otelProvider;
        if (providerImpl != null && MeterProvider.class.isAssignableFrom(providerImpl.getClass())) {
            otelProvider = (MeterProvider) providerImpl;
        } else {
            otelProvider = DEFAULT_PROVIDER;
        }

        this.isEnabled = otelProvider != MeterProvider.noop();

        this.meter = otelProvider.meterBuilder(libraryName)
            .setInstrumentationVersion(libraryVersion)
            .build();
    }

    @Override
    public ClientLongHistogram getLongHistogram(String metricName, String metricDescription, String unit, Map<String, Object> attributes) {
        LongHistogramBuilder otelMetricBuilder = meter.histogramBuilder(metricName)
            .setDescription(metricDescription)
            .ofLongs();
        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryLongHistogram(otelMetricBuilder.build(), attributes);
    }

    @Override
    public ClientLongCounter getLongCounter(String metricName, String metricDescription, String unit, Map<String, Object> attributes) {
        LongCounterBuilder otelMetricBuilder = meter.counterBuilder(metricName).setDescription(metricDescription);

        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryLongCounter(otelMetricBuilder.build(), attributes);
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }
}
