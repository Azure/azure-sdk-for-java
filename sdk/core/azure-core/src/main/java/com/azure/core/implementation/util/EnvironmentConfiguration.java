// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Contains environment (system properties and environment variables) configuration information that is
 * used during construction of client libraries.
 */
public class EnvironmentConfiguration {
    /*
     * Configurations that are loaded into the global configuration store when the application starts.
     */
    private static final Set<String> DEFAULT_CONFIGURATIONS = new HashSet<>(Arrays.asList(
        Configuration.PROPERTY_HTTP_PROXY,
        Configuration.PROPERTY_HTTPS_PROXY,
        Configuration.PROPERTY_IDENTITY_ENDPOINT,
        Configuration.PROPERTY_IDENTITY_HEADER,
        Configuration.PROPERTY_NO_PROXY,
        Configuration.PROPERTY_MSI_ENDPOINT,
        Configuration.PROPERTY_MSI_SECRET,
        Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID,
        Configuration.PROPERTY_AZURE_USERNAME,
        Configuration.PROPERTY_AZURE_PASSWORD,
        Configuration.PROPERTY_AZURE_CLIENT_ID,
        Configuration.PROPERTY_AZURE_CLIENT_SECRET,
        Configuration.PROPERTY_AZURE_TENANT_ID,
        Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH,
        Configuration.PROPERTY_AZURE_IDENTITY_DISABLE_CP1,
        Configuration.PROPERTY_AZURE_RESOURCE_GROUP,
        Configuration.PROPERTY_AZURE_CLOUD,
        Configuration.PROPERTY_AZURE_AUTHORITY_HOST,
        Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED,
        Configuration.PROPERTY_AZURE_LOG_LEVEL,
        Configuration.PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL,
        Configuration.PROPERTY_AZURE_TRACING_DISABLED,
        Configuration.PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL,
        Configuration.PROPERTY_AZURE_REGIONAL_AUTHORITY_NAME,
        Configuration.PROPERTY_AZURE_REQUEST_RETRY_COUNT,
        Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT,
        Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT,
        Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT,
        Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT
    ));

    private static final EnvironmentConfiguration GLOBAL_CONFIGURATION = new EnvironmentConfiguration();

    private final ConcurrentMap<String, Optional<String>> configurations;

    /**
     * Constructs a configuration containing the known Azure properties constants.
     */
    public EnvironmentConfiguration() {
        this.configurations = loadBaseConfiguration();
    }

    /**
     * Clones original configuration.
     */
    public EnvironmentConfiguration(EnvironmentConfiguration original) {
        this.configurations = new ConcurrentHashMap<>(original.configurations);
    }

    /**
     * Constructs a configuration containing mocked environment. Use this constructor for testing.
     */
    public EnvironmentConfiguration(Map<String, String> configurations) {
        Objects.requireNonNull(configurations, "'configurations' can't be null");
        this.configurations = new ConcurrentHashMap<>(configurations.size());
        for (Map.Entry<String, String> config : configurations.entrySet()) {
            this.configurations.put(config.getKey(), Optional.ofNullable(config.getValue()));
        }
    }

    public static EnvironmentConfiguration getGlobalConfiguration() {
        return GLOBAL_CONFIGURATION;
    }

    /**
     * Gets the value of the configuration.
     * <p>
     * This method first checks the values previously loaded from the environment, if the configuration is found there
     * it will be returned. Otherwise, this will attempt to load the value from the environment.
     *
     * @param name Name of the configuration.
     * @return Value of the configuration if found, otherwise null.
     */
    public String get(String name) {
        return getOrLoad(name);
    }

    /**
     * Gets the value of the configuration converted to {@code T}.
     * <p>
     * This method first checks the values previously loaded from the environment, if the configuration is found there
     * it will be returned. Otherwise, this will attempt to load the value from the environment.
     * <p>
     * If no configuration is found, the {@code defaultValue} is returned.
     *
     * @param name Name of the configuration.
     * @param defaultValue Value to return if the configuration isn't found.
     * @param <T> Type that the configuration is converted to if found.
     * @return The converted configuration if found, otherwise the default value is returned.
     */
    public <T> T get(String name, T defaultValue) {
        return ConfigurationUtils.convertToPrimitiveOrDefault(getOrLoad(name), defaultValue);
    }

    /**
     * Gets the value of the configuration and converts it with the {@code converter}.
     * <p>
     * This method first checks the values previously loaded from the environment, if the configuration is found there
     * it will be returned. Otherwise, this will attempt to load the value from the environment.
     * <p>
     * If no configuration is found the {@code converter} won't be called and null will be returned.
     *
     * @param name Name of the configuration.
     * @param converter Converter used to map the configuration to {@code T}.
     * @param <T> Type that the configuration is converted to if found.
     * @return The converted configuration if found, otherwise null.
     */
    public <T> T get(String name, Function<String, T> converter) {
        Objects.requireNonNull(converter, "'converter' can't be null");
        String value = getOrLoad(name);
        if (value == null) {
            return null;
        }

        return converter.apply(value);
    }

    /*
     * Attempts to get the value of the configuration from the configuration store, if the value isn't found then it
     * attempts to load it from the runtime parameters then the environment variables.
     *
     * If no configuration is found null is returned.
     *
     * @param name Name of the configuration.
     * @return The configuration value from either the configuration store, runtime parameters, or environment
     * variable, in that order, if found, otherwise null.
     */
    private String getOrLoad(String name) {
        Optional<String> value = configurations.get(name);
        if (value != null) {
            return value.orElse(null);
        }

        String envValue = load(name);
        configurations.put(name, Optional.ofNullable(envValue));
        return envValue;
    }

    /**
     * Adds a configuration with the given value.
     * <p>
     * This will overwrite the previous configuration value if it existed.
     *
     * @param name  Name of the configuration.
     * @param value Value of the configuration.
     * @return The updated Configuration object.
     */
    public EnvironmentConfiguration put(String name, String value) {
        configurations.put(name, Optional.ofNullable(value));
        return this;
    }

    /**
     * Removes the configuration.
     * <p>
     * This returns the value of the configuration if it previously existed.
     *
     * @param name Name of the configuration.
     * @return The configuration if it previously existed, otherwise null.
     */
    public String remove(String name) {
        Optional<String> value = configurations.remove(name);
        return (value != null && value.isPresent()) ? value.get() : null;
    }

    /**
     * Determines if the configuration exists.
     * <p>
     * This only checks against values previously loaded into the Configuration object, this won't inspect the
     * environment for containing the value.
     *
     * @param name Name of the configuration.
     * @return True if the configuration exists, otherwise false.
     */
    public boolean contains(String name) {
        Optional<String> value = configurations.get(name);
        return value != null && value.isPresent();
    }

    private String load(String propertyName) {
        String value = loadFromProperties(propertyName);

        if (value != null) {
            return value;
        }

        return loadFromEnvironment(propertyName);
    }

    String loadFromEnvironment(String name) {
        return System.getenv(name);
    }

    String loadFromProperties(String name) {
        return System.getProperty(name);
    }

    private ConcurrentMap<String, Optional<String>> loadBaseConfiguration() {
        ConcurrentMap<String, Optional<String>> configurations = new ConcurrentHashMap<>();
        for (String config : DEFAULT_CONFIGURATIONS) {
            String value = load(config);
            if (value != null) {
                configurations.put(config, Optional.of(value));
            }
        }

        return configurations;
    }
}
