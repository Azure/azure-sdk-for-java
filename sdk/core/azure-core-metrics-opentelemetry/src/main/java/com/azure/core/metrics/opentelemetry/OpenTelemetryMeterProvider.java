package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.AzureMeter;
import com.azure.core.util.metrics.AzureMeterProvider;

public class OpenTelemetryMeterProvider implements AzureMeterProvider {

    public OpenTelemetryMeterProvider() {
    }

    @Override
    public AzureMeter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        return new OpenTelemetryMeter(libraryName, libraryVersion, options);
    }
}
