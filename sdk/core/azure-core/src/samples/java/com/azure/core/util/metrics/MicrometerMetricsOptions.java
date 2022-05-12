// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.MetricsOptions;
import io.micrometer.core.instrument.MeterRegistry;

public class MicrometerMetricsOptions extends MetricsOptions {
    private MeterRegistry registry;

    /**
     * Gets implementation-specific state containing all configuration needed for the implementation such as OpenTelemetry MeterProvider.
     * Check out OpenTelemetry Metrics plugin documentation for the details and examples.
     *
     * @return the value of implementation-specific metric provider, {@code null} by default.
     */
    public MeterRegistry getRegistry() {
        return registry;
    }

    /**
     * Sets implementation-specific state containing all configuration needed for the implementation such as OpenTelemetry MeterProvider.
     * Check out OpenTelemetry Metrics plugin documentation for the details and examples.
     *
     * @param registry Instance of {@link MeterRegistry}.
     * @return the updated {@code MetricsOptions} object.
     */
    public MicrometerMetricsOptions setRegistry(MeterRegistry registry) {
        this.registry = registry;
        return this;
    }
}
