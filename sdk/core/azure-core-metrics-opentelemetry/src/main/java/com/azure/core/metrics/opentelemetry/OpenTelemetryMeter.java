package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.AzureLongCounter;
import com.azure.core.util.metrics.AzureLongHistogram;
import com.azure.core.util.metrics.AzureMeter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;

import java.util.Map;

public class OpenTelemetryMeter implements AzureMeter {
    private final static io.opentelemetry.api.metrics.MeterProvider DEFAULT_PROVIDER = GlobalOpenTelemetry.getMeterProvider();
    private final Meter meter;
    private final boolean isEnabled;

    public OpenTelemetryMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        MeterProvider otelProvider = DEFAULT_PROVIDER;
        if (MeterProvider.class.isAssignableFrom(options.getProvider().getClass())) {
            otelProvider = (MeterProvider) options.getProvider();
        }

        this.isEnabled = otelProvider != MeterProvider.noop();

        this.meter = otelProvider.meterBuilder(libraryName)
            .setInstrumentationVersion(libraryVersion)
            .build();
    }

    @Override
    public AzureLongHistogram createLongHistogram(String name, String description, String unit, Map<String, Object> attributes) {
        LongHistogramBuilder otelMetricBuilder = meter.histogramBuilder(name)
            .setDescription(description)
            .ofLongs();
        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryLongHistogram(otelMetricBuilder.build(), attributes);
    }

    @Override
    public AzureLongCounter createLongCounter(String name, String description, String unit, Map<String, Object> attributes) {
        LongCounterBuilder otelMetricBuilder = meter.counterBuilder(name)
            .setDescription(description);

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
