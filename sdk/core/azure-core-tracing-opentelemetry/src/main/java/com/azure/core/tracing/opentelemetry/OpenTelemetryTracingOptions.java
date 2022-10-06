// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.TracingOptions;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;

/**
 * OpenTelemetry-specific Azure SDK tracing options.
 */
public class OpenTelemetryTracingOptions extends TracingOptions {
    private TracerProvider provider;
    private OpenTelemetrySchemaVersion schemaVersion;

    /**
     * Gets implementation-specific state containing all configuration needed for the implementation such as OpenTelemetry MeterProvider.
     * Check out OpenTelemetry Metrics plugin documentation for the details and examples.
     *
     * @return the value of implementation-specific metric provider, {@code null} by default.
     */
    public TracerProvider getProvider() {
        return provider;
    }

    /**
     * Sets implementation-specific state containing all configuration needed for the implementation such as OpenTelemetry MeterProvider.
     * Check out OpenTelemetry Metrics plugin documentation for the details and examples.
     *
     * @param provider Instance of {@link MeterProvider}
     * @return the updated {@code MetricsOptions} object.
     */
    public OpenTelemetryTracingOptions setProvider(TracerProvider provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Gets schema version.
     * @return schema version.
     */
    public OpenTelemetrySchemaVersion getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Sets schema version.
     * @param schemaVersion schema version.
     * @return updated OpenTelemetryTracingOptions.
     */
    public OpenTelemetryTracingOptions setSchemaVersion(OpenTelemetrySchemaVersion schemaVersion) {
        this.schemaVersion = schemaVersion;
        return this;
    }
}
