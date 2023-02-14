// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

/**
 * Metrics configuration options for clients.
 */
public class TracingOptions {
    private static final ConfigurationProperty<Boolean> IS_DISABLED_PROPERTY = ConfigurationPropertyBuilder
        .ofBoolean("tracing.disabled")
        .environmentVariableName(Configuration.PROPERTY_AZURE_TRACING_DISABLED)
        .shared(true)
        .defaultValue(false)
        .build();

    private boolean isEnabled;

    /**
     * Creates new instance of {@link TracingOptions}
     */
    public TracingOptions() {
        isEnabled = !Configuration.getGlobalConfiguration().get(IS_DISABLED_PROPERTY);
    }

    /**
     * Loads metrics options from the configuration.
     *
     *
     * @param configuration The {@link Configuration} that is used to load proxy configurations from the environment. If
     * {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @return A {@link TracingOptions} reflecting a tracing options loaded from the configuration,
     * if no tracing options are found, default (enabled) tracing options will be returned.
     */
    public static TracingOptions fromConfiguration(Configuration configuration) {
        if (configuration.contains(IS_DISABLED_PROPERTY)) {
            return new TracingOptions().setEnabled(!configuration.get(IS_DISABLED_PROPERTY));
        }

        return new TracingOptions();
    }

    /**
     * Flag indicating if metrics should be enabled.
     * @return {@code true} if metrics are enabled, {@code false} otherwise.
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Enables or disables metrics. By default, metrics are enabled if and only if metrics implementation is detected.
     *
     * @param enabled pass {@code true} to enable metrics.
     * @return the updated {@code TracingOptions} object.
     */
    public TracingOptions setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }
}
