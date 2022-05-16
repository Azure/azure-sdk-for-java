package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.ClientLongCounter;
import com.azure.core.util.metrics.ClientLongHistogram;
import com.azure.core.util.metrics.ClientMeter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;

import java.util.Map;

public class OpenTelemetryMeter implements ClientMeter {
    private final static io.opentelemetry.api.metrics.MeterProvider DEFAULT_PROVIDER = GlobalOpenTelemetry.getMeterProvider();
    private final Meter meter;

    public OpenTelemetryMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        Object providerImpl = options.getImplementationConfiguration();
        MeterProvider otelProvider;
        if (providerImpl != null && MeterProvider.class.isAssignableFrom(providerImpl.getClass())) {
            otelProvider = (MeterProvider) providerImpl;
        } else {
            otelProvider = DEFAULT_PROVIDER;
        }

        this.meter = otelProvider.meterBuilder(libraryName)
            .setInstrumentationVersion(libraryVersion)
            .build();
    }

    @Override
    public ClientLongHistogram getLongHistogram(String metricName, String metricDescription, String unit, Map<String, Object> attributes) {
        return new OpenTelemetryLongHistogram(meter.histogramBuilder(metricName).setDescription(metricDescription).setUnit(unit).ofLongs().build(), attributes);
    }

    @Override
    public ClientLongCounter getLongCounter(String metricName, String metricDescription, String unit, Map<String, Object> attributes) {
        return new OpenTelemetryLongCounter(meter.counterBuilder(metricName).setDescription(metricDescription).setUnit(unit).build(), attributes);
    }
}
