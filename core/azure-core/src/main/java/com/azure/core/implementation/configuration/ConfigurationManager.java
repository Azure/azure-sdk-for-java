// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;

import java.util.function.Function;

/**
 * Manages the global configuration store.
 */
public final class ConfigurationManager {
    private static final ServiceLogger LOGGER = new ServiceLogger(ConfigurationManager.class);
    private static Configuration configuration = new Configuration();

    /**
     * Gets the value of the configuration.
     *
     * @param name Name of the configuration.
     * @return Value of the configuration if found, otherwise null.
     */
    public static String get(String name) {
        return getOrLoad(name);
    }

    /**
     * Gets the value of the configuration converted to {@code T}.
     *
     * If no configuration is found the default is returned.
     *
     * @param name Name of the configuration.
     * @param defaultValue Value to return if the configuration isn't found.
     * @param <T> Generic type that the configuration is converted to if found.
     * @return The converted configuration if found, otherwise the default value is returned.
     */
    public static <T> T get(String name, T defaultValue) {
        return Configuration.convertOrDefault(getOrLoad(name), defaultValue);
    }

    /**
     * Gets the converted value of the configuration.
     *
     * @param name Name of the configuration.
     * @param converter Converter used to map the configuration to {@code T}.
     * @param <T> Generic type that the configuration is converted to if found.
     * @return The converted configuration if found, otherwise null.
     */
    public static <T> T get(String name, Function<String, T> converter) {
        return converter.apply(getOrLoad(name));
    }

    /**
     * @return a mutable clone of the global configuration store.
     */
    public static Configuration configuration() {
        return configuration.clone();
    }

    /**
     * First attempts to retrieve the configuration from the global configuration store.
     *
     * If not found in the store then the runtime parameters and environment variables are checked for the configuration,
     * if found the value is loaded into the store.
     *
     * @param name Name of the configuration.
     * @return Value of the configuration from either the global store, runtime parameters, or environment variables,
     * check in that order. If the configuration value is not found then null.
     */
    private static String getOrLoad(String name) {
        if (configuration.contains(name)) {
            return configuration.get(name);
        }

        String value = System.getProperty(name);
        if (!ImplUtils.isNullOrEmpty(value)) {
            configuration.put(name, value);
            LOGGER.asInformational().log("Found configuration {} in the runtime parameters.", name);
            return value;
        }

        value = System.getenv(name);
        if (!ImplUtils.isNullOrEmpty(value)) {
            configuration.put(name, value);
            LOGGER.asInformational().log("Found configuration {} in the environment variables.", name);
            return value;
        }

        return null;
    }
}
