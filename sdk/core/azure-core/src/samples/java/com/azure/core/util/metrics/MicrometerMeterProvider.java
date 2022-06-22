// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.MetricsOptions;

/**
 * Sample implementation of Micrometer {@link AzureMeterProvider}. Should be resolved using SPI and registered in provider
 * configuration file.
 */
public class MicrometerMeterProvider implements AzureMeterProvider {

    /**
     * Default constructor for {@link java.util.ServiceLoader#load(Class, ClassLoader)}
     */
    public MicrometerMeterProvider() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureMeter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        return new MicrometerMeter(libraryName, libraryVersion, options);
    }
}
