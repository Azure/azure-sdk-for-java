// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.core.util.MetricsOptions;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Micrometer-specific Azure Cosmos DB SDK metrics options
 */
public final class CosmosMicrometerMetricsOptions extends MetricsOptions {
    private MeterRegistry clientMetricRegistry = null;

    /**
     * Instantiates new Micrometer-specific Azure Cosmos DB SDK metrics options
     */
    public CosmosMicrometerMetricsOptions() {
    }

    MeterRegistry getClientMetricRegistry() {
        return this.clientMetricRegistry;
    }

    /**
     * Sets MetricRegistry to be used to emit client metrics
     *
     * @param clientMetricMeterRegistry - the MetricRegistry to be used to emit client metrics
     * @return current CosmosMicrometerMetricsOptions instance
     */
    public CosmosMicrometerMetricsOptions meterRegistry(MeterRegistry clientMetricMeterRegistry) {
        this.clientMetricRegistry = clientMetricMeterRegistry;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CosmosMicrometerMetricsOptions setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return this.clientMetricRegistry != null && super.isEnabled();
    }
}
