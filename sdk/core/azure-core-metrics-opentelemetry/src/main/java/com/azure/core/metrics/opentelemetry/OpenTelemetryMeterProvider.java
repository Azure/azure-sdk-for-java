package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.MeterProvider;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;

public class OpenTelemetryMeterProvider implements MeterProvider<io.opentelemetry.api.metrics.MeterProvider> {
    private final static io.opentelemetry.api.metrics.MeterProvider DEFAULT_PROVIDER = GlobalOpenTelemetry.getMeterProvider();
    @Override
    public DoubleHistogram getDoubleHistogram(String metricName, String metricDescription, String unit, MetricsOptions<io.opentelemetry.api.metrics.MeterProvider> options) {
        io.opentelemetry.api.metrics.MeterProvider otelProvider = options.getProviderImplementation();
        if (otelProvider == null) {
            otelProvider = DEFAULT_PROVIDER;
        }

        Meter meter = otelProvider.meterBuilder(options.getInstrumentationScope()).setInstrumentationVersion(options.getInstrumentationVersion()).build();
        return new OpenTelemetryDoubleHistogram(meter.histogramBuilder(metricName).setDescription(metricDescription).setUnit(unit).build());
    }
}
