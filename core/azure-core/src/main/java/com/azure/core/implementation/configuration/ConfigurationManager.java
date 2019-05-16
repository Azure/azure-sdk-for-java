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
    private static final ServiceLogger logger = new ServiceLogger(ConfigurationManager.class);
    private static Configuration configuration = new Configuration();

    /**
     * Gets the value of the configuration.
     *
     * @param name Name of the configuration.
     * @return Value of the configuration if found, otherwise null.
     */
    public static String get(String name) {
        return loadConfiguration(name);
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
        return Configuration.convertOrDefault(loadConfiguration(name), defaultValue);
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
        return converter.apply(loadConfiguration(name));
    }

    /**
     * @return a mutable clone of the global configuration store.
     */
    public static Configuration configuration() {
        return configuration.clone();
    }

    private static String loadConfiguration(String name) {
        if (configuration.contains(name)) {
            return configuration.get(name);
        }

        String value = System.getProperty(name);
        if (!ImplUtils.isNullOrEmpty(value)) {
            configuration.put(name, value);
            logger.asInformational().log("Found configuration {} in the runtime parameters.", name);
            return value;
        }

        value = System.getenv(name);
        if (!ImplUtils.isNullOrEmpty(value)) {
            configuration.put(name, value);
            logger.asInformational().log("Found configuration {} in the environment variables.", name);
            return value;
        }

        return null;
    }
}
