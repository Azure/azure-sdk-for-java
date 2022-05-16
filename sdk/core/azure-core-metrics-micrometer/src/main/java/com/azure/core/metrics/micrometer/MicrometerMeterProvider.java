package com.azure.core.metrics.micrometer;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.ClientMeter;
import com.azure.core.util.metrics.ClientMeterProvider;

public class MicrometerMeterProvider extends ClientMeterProvider {

    public MicrometerMeterProvider() {
    }

    @Override
    public ClientMeter getMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        return new MicrometerMeter(libraryName, libraryVersion, options);
    }
}
