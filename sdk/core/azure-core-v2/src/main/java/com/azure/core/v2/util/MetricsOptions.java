// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util;

import com.azure.core.v2.util.metrics.Meter;
import com.azure.core.v2.util.metrics.MeterProvider;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.configuration.ConfigurationProperty;
import io.clientcore.core.util.configuration.ConfigurationPropertyBuilder;

import static io.clientcore.core.implementation.util.ImplUtils.getClassByName;

/**
 * Metrics configuration options for clients.
 */
public class MetricsOptions {
    private static final ConfigurationProperty<Boolean> IS_DISABLED_PROPERTY
        = ConfigurationPropertyBuilder.ofBoolean("metrics.disabled")
            //.environmentVariableName(Configuration.PROPERTY_AZURE_METRICS_DISABLED)
            .shared(true)
            .defaultValue(false)
            .build();

    private static final ConfigurationProperty<String> PROVIDER_NAME_PROPERTY
        = ConfigurationPropertyBuilder.ofString("metrics.provider.implementation")
            //.environmentVariableName(Configuration.PROPERTY_AZURE_METRICS_IMPLEMENTATION)
            .shared(true)
            .build();
    private static final Configuration GLOBAL_CONFIG = Configuration.getGlobalConfiguration();
    private final Class<? extends MeterProvider> meterProvider;
    private boolean isEnabled;

    /**
     * Creates new instance of {@link MetricsOptions}
     */
    public MetricsOptions() {
        this(GLOBAL_CONFIG);
    }

    /**
     * Creates new instance of {@link MetricsOptions}
     *
     * @param meterProvider type of the {@link MeterProvider} implementation that should be used to construct an instance of
     * {@link Meter}.
     * If the value is not set (or {@code null}), then the first {@link MeterProvider} resolved by {@link java.util.ServiceLoader} will
     * be used to create an instance of {@link Meter}. If the value is set and doesn't match any
     * {@link MeterProvider} resolved by {@link java.util.ServiceLoader} an {@link IllegalStateException} will be thrown when
     *  attempting to create an instance of {@link Meter}.
     */
    protected MetricsOptions(Class<? extends MeterProvider> meterProvider) {
        this.isEnabled = !Boolean.parseBoolean(GLOBAL_CONFIG.get(String.valueOf(IS_DISABLED_PROPERTY)));
        this.meterProvider = meterProvider;
    }

    private MetricsOptions(Configuration configuration) {
        isEnabled = Boolean.parseBoolean(configuration.get(String.valueOf(IS_DISABLED_PROPERTY)));
        String className = configuration.get(String.valueOf(PROVIDER_NAME_PROPERTY));
        meterProvider = className != null ? getClassByName(className) : null;
    }

    /**
     * Attempts to load metrics options from the configuration.
     *
     * @param configuration The {@link Configuration} instance containing metrics options. If
     * {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @return A {@link MetricsOptions} reflecting a metrics loaded from configuration, if no options are found, default
     * (enabled) options will be returned.
     */
    public static MetricsOptions fromConfiguration(Configuration configuration) {
        if (configuration.contains(String.valueOf(IS_DISABLED_PROPERTY))) {
            return new MetricsOptions()
                .setEnabled(!Boolean.parseBoolean(configuration.get(String.valueOf(IS_DISABLED_PROPERTY))));
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
     * @return the updated {@link MetricsOptions} object.
     */
    public MetricsOptions setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    /**
     * Gets configured {@link MeterProvider} implementation that should be used to construct an instance of
     * {@link Meter}.
     *
     * @return The {@link MeterProvider} implementation used to create an instance of {@link Meter}.
     */
    public Class<? extends MeterProvider> getMeterProvider() {
        return meterProvider;
    }
}
