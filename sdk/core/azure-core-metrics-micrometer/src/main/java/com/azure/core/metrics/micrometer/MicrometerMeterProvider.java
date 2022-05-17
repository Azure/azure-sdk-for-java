package com.azure.core.metrics.micrometer;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.AzureMeter;
import com.azure.core.util.metrics.AzureMeterProvider;

public class MicrometerMeterProvider implements AzureMeterProvider {

    public MicrometerMeterProvider() {
    }

    @Override
    public AzureMeter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        return new MicrometerMeter(libraryName, libraryVersion, options);
    }
}
