// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils.configuration;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpClientProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains configuration information that is used during construction of client libraries.
 */
public final class Configuration {
    // Default properties - these are what we read from the environment
    /**
     * URI of the proxy for HTTP connections.
     */
    public static final String HTTP_PROXY = "HTTP_PROXY";

    /**
     * URI of the proxy for HTTPS connections.
     */
    public static final String HTTPS_PROXY = "HTTPS_PROXY";

    /**
     * A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through.
     */
    public static final String NO_PROXY = "NO_PROXY";

    /**
     * Enables logging by setting a log level.
     */
    public static final String LOG_LEVEL = "LOG_LEVEL";

    /**
     * Enables HTTP request/response logging by setting an HTTP log level.
     */
    public static final String HTTP_LOG_LEVEL = "HTTP_LOG_LEVEL";

    /**
     * Sets the default number of times a request will be retried, if it passes the conditions for retrying, before it
     * fails.
     */
    public static final String MAX_RETRY_ATTEMPTS = "MAX_RETRY_ATTEMPTS";

    /**
     * Sets the default timeout, in milliseconds, for a request to connect to the remote host.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String REQUEST_CONNECT_TIMEOUT_IN_MS = "REQUEST_CONNECT_TIMEOUT_IN_MS";

    /**
     * Sets the default timeout interval, in milliseconds, allowed between each byte written by a request.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String REQUEST_WRITE_TIMEOUT_IN_MS = "REQUEST_WRITE_TIMEOUT_IN_MS";

    /**
     * Sets the default timeout, in milliseconds, for a request to receive a response from the remote host.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String REQUEST_RESPONSE_TIMEOUT_IN_MS = "REQUEST_RESPONSE_TIMEOUT_IN_MS";

    /**
     * Sets the default timeout interval, in milliseconds, allowed between each byte read in a response.
     * <p>
     * If the configured value is equal to or less than 0 no timeout will be applied.
     */
    public static final String REQUEST_READ_TIMEOUT_IN_MS = "REQUEST_READ_TIMEOUT_IN_MS";

    /**
     * Sets the name of the {@link HttpClientProvider} implementation that should be used to construct instances of
     * {@link HttpClient}.
     * <p>
     * The name must be the full class name, ex {@code io.clientcore.http.okhttp3.OkHttpHttpClientProvider} and not
     * {@code OkHttpHttpClientProvider}, to disambiguate multiple providers with the same name but from different
     * packages.
     * <p>
     * If the value isn't set or is an empty string the first {@link HttpClientProvider} resolved by
     * {@link java.util.ServiceLoader} will be used to create an instance of {@link HttpClient}. If the value is set
     * and doesn't match any {@link HttpClientProvider} resolved by {@link java.util.ServiceLoader} an
     * {@link IllegalStateException} will be thrown when attempting to create an instance of {@link HttpClient}.
     */
    public static final String HTTP_CLIENT_IMPLEMENTATION = "HTTP_CLIENT_IMPLEMENTATION";

    /*
     * The global configuration shared by all client libraries.
     */
    private static final Configuration GLOBAL_CONFIGURATION
        = Configuration.from(new SystemPropertiesConfigurationSource(), new EnvironmentVariableConfigurationSource());

    /*
     * The configuration that never returns any configuration values.
     */
    private static final Configuration NONE = new Configuration(Collections.emptyList());

    private final List<ConfigurationSource> sources;

    private Configuration(List<ConfigurationSource> sources) {
        this.sources = sources;
    }

    /**
     * Creates an instance of {@link Configuration} with the given {@link ConfigurationSource}.
     * <p>
     * The {@code sources} will be queried in the order they are provided.
     * <p>
     * If {@code sources} are empty, all calls to get configurations will return null.
     *
     * @param sources The configuration sources to use.
     * @return an instance of {@link Configuration} initialized with the provided configuration sources.
     * @throws IllegalArgumentException If the sources are <code>null</code> or any entry in the sources array is <code>null</code>.
     */
    public static Configuration from(ConfigurationSource... sources) {
        if (sources == null) {
            throw new IllegalArgumentException("Sources cannot be null");
        }

        if (sources.length == 0) {
            return NONE;
        }

        List<ConfigurationSource> sourceList = new ArrayList<>(sources.length);
        for (ConfigurationSource source : sources) {
            if (source == null) {
                throw new IllegalArgumentException("Source cannot be null");
            }
            sourceList.add(source);
        }
        return new Configuration(sourceList);
    }

    /**
     * Gets the global configuration store shared by all client libraries.
     * <p>
     * The global configuration comprises two sources, queried in this order:
     * <ol>
     *   <li>System properties</li>
     *   <li>Environment variables</li>
     * </ol>
     *
     *
     * @return The global configuration store.
     */
    public static Configuration getGlobalConfiguration() {
        return GLOBAL_CONFIGURATION;
    }

    /**
     * Gets the configuration store that never returns configuration values.
     *
     * @return The configuration store that never returns configuration values.
     */
    public static Configuration none() {
        return NONE;
    }

    /**
     * Gets the configuration value associated with {@code name}.
     * <p>
     * If {@code name} is null, null will be returned.
     *
     * @param name Name of the configuration.
     * @return Value of the configuration if found, otherwise null.
     */
    public String get(String name) {
        if (sources == null || name == null) {
            return null;
        }

        return getInternal(name);
    }

    /**
     * Gets the configuration value associated with {@code name}.
     * <p>
     * If {@code name} is null, it will be ignored and {@code aliases} will be used. Any null aliases will be ignored.
     *
     * @param name Name of the configuration.
     * @param aliases Aliases of the configuration.
     * @return Value of the configuration if found, otherwise null.
     */
    public String get(String name, String... aliases) {
        if (sources == null) {
            return null;
        }

        String value = getInternal(name);
        if (value != null) {
            return value;
        }

        if (aliases != null) {
            for (String alias : aliases) {
                value = getInternal(alias);
                if (value != null) {
                    return value;
                }
            }
        }

        return null;
    }

    private String getInternal(String name) {
        if (name == null) {
            return null;
        }

        for (ConfigurationSource source : sources) {
            String value = source.getProperty(name);
            if (value != null) {
                return value;
            }
        }

        return null;
    }
}
