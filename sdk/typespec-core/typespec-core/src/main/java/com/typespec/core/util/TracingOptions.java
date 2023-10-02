// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import com.typespec.core.util.tracing.Tracer;
import com.typespec.core.util.tracing.TracerProvider;

import static com.typespec.core.implementation.ImplUtils.getClassByName;

/**
 * Tracing configuration options for clients.
 */
public class TracingOptions {
    private static final ConfigurationProperty<Boolean> IS_DISABLED_PROPERTY = ConfigurationPropertyBuilder.ofBoolean("tracing.disabled")
        .environmentVariableName(Configuration.PROPERTY_AZURE_TRACING_DISABLED)
        .shared(true)
        .defaultValue(false)
        .build();

    private static final ConfigurationProperty<String> PROVIDER_NAME_PROPERTY = ConfigurationPropertyBuilder.ofString("tracing.provider.implementation")
        .environmentVariableName(Configuration.PROPERTY_AZURE_TRACING_IMPLEMENTATION)
        .shared(true)
        .build();

    private static final Configuration GLOBAL_CONFIG = Configuration.getGlobalConfiguration();
    private final Class<? extends TracerProvider> tracerProvider;
    private boolean isEnabled;

    /**
     * Creates new instance of {@link TracingOptions}
     */
    public TracingOptions() {
        this(GLOBAL_CONFIG);
    }

    /**
     * Creates new instance of {@link TracingOptions}
     *
     * @param tracerProvider The type of the {@link TracerProvider} implementation that should be used to construct an instance of
     * {@link Tracer}.
     *
     * If the value isn't set or is an empty string the first {@link TracerProvider} resolved by {@link java.util.ServiceLoader} will
     * be used to create an instance of {@link Tracer}. If the value is set and doesn't match any
     * {@link TracerProvider}resolved by {@link java.util.ServiceLoader} an {@link IllegalStateException} will be thrown when
     *  attempting to create an instance of {@link Tracer}.
     *
     */
    protected TracingOptions(Class<? extends TracerProvider> tracerProvider) {
        this.tracerProvider = tracerProvider;
        this.isEnabled = !GLOBAL_CONFIG.get(IS_DISABLED_PROPERTY);
    }

    private TracingOptions(Configuration configuration) {
        isEnabled = !configuration.get(IS_DISABLED_PROPERTY);
        String className = configuration.get(PROVIDER_NAME_PROPERTY);
        tracerProvider = className != null ? getClassByName(className) : null;
    }

    /**
     * Loads tracing options from the configuration.
     *
     *
     * @param configuration The {@link Configuration} that is used to load proxy configurations from the environment. If
     * {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @return A {@link TracingOptions} reflecting a tracing options loaded from the configuration,
     * if no tracing options are found, default (enabled) tracing options will be returned.
     */
    public static TracingOptions fromConfiguration(Configuration configuration) {
        return new TracingOptions(configuration);
    }

    /**
     * Flag indicating if distributed tracing should be enabled.
     * @return {@code true} if tracing is enabled, {@code false} otherwise.
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Enables or disables distributed tracing. By default, tracing is enabled if and only if tracing implementation is detected.
     *
     * @param enabled pass {@code true} to enable tracing.
     * @return the updated {@code TracingOptions} object.
     */
    public TracingOptions setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    /**
     * Gets name of the {@link TracerProvider} implementation that should be used to construct an instance of
     * {@link Tracer}.
     *
     * @return The {@link TracerProvider} implementation used to create an instance of {@link Tracer}.
     */
    public Class<? extends TracerProvider> getTracerProvider() {
        return tracerProvider;
    }
}
