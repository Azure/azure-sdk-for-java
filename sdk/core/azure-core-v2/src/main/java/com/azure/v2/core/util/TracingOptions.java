// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.util;

import com.azure.v2.core.util.tracing.Tracer;
import com.azure.v2.core.util.tracing.TracerProvider;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.configuration.Configuration;
import io.clientcore.core.utils.configuration.ConfigurationProperty;
import io.clientcore.core.utils.configuration.ConfigurationPropertyBuilder;

import java.util.Objects;
import java.util.Set;

/**
 * Tracing configuration options for clients.
 */
public class TracingOptions {
    private static final ClientLogger LOGGER = new ClientLogger(TracingOptions.class);

    private static final ConfigurationProperty<Boolean> IS_DISABLED_PROPERTY
        = ConfigurationPropertyBuilder.ofBoolean("tracing.disabled")
            //.environmentVariableName(Configuration.PROPERTY_AZURE_TRACING_DISABLED)
            .shared(true)
            .defaultValue(false)
            .build();

    private static final ConfigurationProperty<String> PROVIDER_NAME_PROPERTY
        = ConfigurationPropertyBuilder.ofString("tracing.provider.implementation")
            //.environmentVariableName(Configuration.PROPERTY_AZURE_TRACING_IMPLEMENTATION)
            .shared(true)
            .build();

    private static final Configuration GLOBAL_CONFIG = Configuration.getGlobalConfiguration();
    private final Class<? extends TracerProvider> tracerProvider;
    private Set<String> allowedQueryParamNames;
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
     * If the value is not set (or {@code null}), then the first {@link TracerProvider} resolved by {@link java.util.ServiceLoader} will
     * be used to create an instance of {@link Tracer}. If the value is set and doesn't match any
     * {@link TracerProvider} resolved by {@link java.util.ServiceLoader} an {@link IllegalStateException} will be thrown when
     *  attempting to create an instance of {@link Tracer}.
     */
    protected TracingOptions(Class<? extends TracerProvider> tracerProvider) {
        this.tracerProvider = tracerProvider;
        this.isEnabled = !Boolean.parseBoolean(GLOBAL_CONFIG.get(String.valueOf(IS_DISABLED_PROPERTY)));
    }

    private TracingOptions(Configuration configuration) {
        isEnabled = Boolean.parseBoolean(configuration.get(String.valueOf(IS_DISABLED_PROPERTY)));
        String className = configuration.get(String.valueOf(PROVIDER_NAME_PROPERTY));
        tracerProvider = className != null ? getClassByName(className) : null;
    }

    /**
     * Gets a {@link Class} from the given {@code className}.
     * <p>
     * This method will attempt to load the class from the current thread's context class loader. If the class cannot be
     * found on the classpath an exception will be thrown. Unlike calling {@link Class#forName(String)} directly, this
     * utility method doesn't throw a checked exception, rather it throws a {@link RuntimeException}.
     *
     * @param <T> The type of class to load.
     * @param className The name of the class to load.
     * @return The {@link Class} with the given {@code className}.
     * @throws RuntimeException If the class cannot be found on the classpath.
     */
    @SuppressWarnings("unchecked")
    static <T> Class<? extends T> getClassByName(String className) {
        Objects.requireNonNull(className, "'className' cannot be null");
        try {
            return (Class<? extends T>) Class.forName(className, false, TracingOptions.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw LOGGER.logThrowableAsError(
                new RuntimeException("Class '" + className + "' is not found on the classpath.", e));
        }
    }

    /**
     * Loads tracing options from the configuration.
     *
     * @param configuration The {@link Configuration} instance containing tracing options. If
     * {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @return A {@link TracingOptions} reflecting updated tracing options loaded from the configuration,
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

    /**
     * Gets the set of query parameter names that are allowed to be recorded in the URL.
     * @return The set of query parameter names that are allowed to be recorded in the URL.
     */
    public Set<String> getAllowedTracingQueryParamNames() {
        return allowedQueryParamNames;
    }

    /**
     * Sets the set of query parameter names that are allowed to be recorded in the URL.
     * @param allowedQueryParamNames The set of query parameter names that are allowed to be recorded in the URL.
     * @return The updated {@link TracingOptions} object.
     */
    public TracingOptions setAllowedTracingQueryParamNames(final Set<String> allowedQueryParamNames) {
        this.allowedQueryParamNames = allowedQueryParamNames;
        return this;
    }
}
