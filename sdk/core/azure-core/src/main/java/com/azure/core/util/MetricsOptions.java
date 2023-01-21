// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

/**
 * Metrics configuration options for clients.
 */
public class MetricsOptions {
    private static final ConfigurationProperty<Boolean> IS_DISABLED_PROPERTY = ConfigurationPropertyBuilder.ofBoolean("metrics.disabled")
        .environmentVariableName(Configuration.PROPERTY_AZURE_METRICS_DISABLED)
        .shared(true)
        .defaultValue(false)
        .build();

    private boolean isEnabled;

    /**
     * Creates new instance of {@link MetricsOptions}
     */
    public MetricsOptions() {
        isEnabled = !Configuration.getGlobalConfiguration().get(IS_DISABLED_PROPERTY);
    }
    /**
     * Attempts to load metrics options from the configuration.
     * <p>
     * {@code null} will be returned if no metric options are found in the environment.
     *
     * @param configuration The {@link Configuration} that is used to load proxy configurations from the environment. If
     * {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @return A {@link MetricsOptions} reflecting a metrics loaded from configuration, if no options are found, default
     * (enabled) options will be returned.
     */
    public static MetricsOptions fromConfiguration(Configuration configuration) {
        if (configuration.contains(IS_DISABLED_PROPERTY)) {
            return new MetricsOptions().setEnabled(!configuration.get(IS_DISABLED_PROPERTY));
        }

        return new MetricsOptions();
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
     * @return the updated {@code MetricsOptions} object.
     */
    public MetricsOptions setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }
}
