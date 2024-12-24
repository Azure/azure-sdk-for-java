// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.observability;

/**
 * Options for configuring observability for a specific application needs.
 * @param <T> The type of the provider. Only io.opentelemetry.api.OpenTelemetry is supported.
 */
public class ObservabilityOptions<T> {
    private boolean isTracingEnabled = true;
    private T provider = null;

    /**
     * Enables or disables distributed tracing. Default is true.
     *
     * @param isTracingEnabled true to enable distributed tracing, false to disable.
     * @return The updated {@link ObservabilityOptions} object.
     */
    public ObservabilityOptions<T> setTracingEnabled(boolean isTracingEnabled) {
        this.isTracingEnabled = isTracingEnabled;
        return this;
    }

    /**
     * Sets the provider to use for observability. Only io.opentelemetry.api.OpenTelemetry is supported.
     *
     * @param provider The provider to use for observability.
     * @return The updated {@link ObservabilityOptions} object.
     */
    public ObservabilityOptions<T> setProvider(T provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Returns true if distributed tracing is enabled, false otherwise.
     *
     * @return true if distributed tracing is enabled, false otherwise.
     */
    public boolean isTracingEnabled() {
        return isTracingEnabled;
    }

    /**
     * Returns the provider to use for observability.
     *
     * @return The provider to use for observability.
     */
    public T getProvider() {
        return provider;
    }

    /**
     * Creates an instance of {@link ObservabilityOptions}.
     */
    public ObservabilityOptions() {
    }
}
