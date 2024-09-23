// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;

public class TestMeterProvider implements MeterProvider {

    private final MeterFactory meterFactory;
    public TestMeterProvider(MeterFactory meterFactory) {
        this.meterFactory = meterFactory;
    }

    @Override
    public Meter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        return meterFactory.createMeter(libraryName, libraryVersion, options);
    }

    @FunctionalInterface
    interface MeterFactory {
        Meter createMeter(String libraryName, String libraryVersion, MetricsOptions options);
    }
}
