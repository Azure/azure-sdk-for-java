package com.azure.core.util.metrics;

import com.azure.core.util.MetricsOptions;

public interface AzureMeterProvider {
    AzureMeter createMeter(String libraryName, String libraryVersion, MetricsOptions options);

    static AzureMeterProvider getDefaultProvider() {
        return DefaultAzureMeterProvider.INSTANCE;
    }
}
