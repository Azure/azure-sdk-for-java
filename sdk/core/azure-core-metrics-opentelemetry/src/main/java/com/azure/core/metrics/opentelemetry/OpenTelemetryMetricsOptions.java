// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.MetricsOptions;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;

/**
 * OpenTelemetry-specific Azure SDK metrics options.
 */
public class OpenTelemetryMetricsOptions extends MetricsOptions {
    private MeterProvider provider;

    /**
     * Creates an instance of {@link OpenTelemetryMetricsOptions}.
     */
    public OpenTelemetryMetricsOptions() {
        super(OpenTelemetryMeterProvider.class);
    }

    /**
     * Gets implementation-specific state containing all configuration needed for the implementation such as OpenTelemetry MeterProvider.
     * Check out OpenTelemetry Metrics plugin documentation for the details and examples.
     *
     * @return the value of implementation-specific metric provider, {@code null} by default.
     */
    MeterProvider getOpenTelemetryProvider() {
        return provider;
    }

    /**
     * Sets implementation-specific state containing all configuration needed for the implementation such as OpenTelemetry MeterProvider.
     * Check out OpenTelemetry Metrics plugin documentation for the details and examples.
     *
     * @param openTelemetry Instance of {@link OpenTelemetry} container
     * @return the updated {@code MetricsOptions} object.
     */
    public OpenTelemetryMetricsOptions setOpenTelemetry(OpenTelemetry openTelemetry) {
        this.provider = openTelemetry.getMeterProvider();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenTelemetryMetricsOptions setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }
}
