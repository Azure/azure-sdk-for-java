// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Contains configuration information that is used during construction of client libraries.
 */
public final class Configuration {

    /**
     * Empty Configuration object used to opt out of using global configurations when constructing clients.
     */
    public static final Configuration NONE = new Configuration();

    private final ServiceLogger logger = new ServiceLogger(Configuration.class);

    private Map<String, String> configurations = new HashMap<>();

    public Configuration() {
    }

    private Configuration(Map<String, String> configurations) {
        this.configurations = new HashMap<>(configurations);
    }

    /**
     * Gets the value of the configuration.
     *
     * @param name Name of the configuration.
     * @return Value of the configuration if found, otherwise null.
     */
    public String get(String name) {
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
    public <T> T get(String name, T defaultValue) {
        return convertOrDefault(getOrLoad(name), defaultValue);
    }

    /**
     * Gets the converted value of the configuration.
     *
     * @param name Name of the configuration.
     * @param converter Converter used to map the configuration to {@code T}.
     * @param <T> Generic type that the configuration is converted to if found.
     * @return The converted configuration if found, otherwise null.
     */
    public <T> T get(String name, Function<String, T> converter) {
        String value = get(name);
        if (ImplUtils.isNullOrEmpty(value)) {
            return null;
        }

        return converter.apply(getOrLoad(name));
    }

    /**
     * Adds the configuration.
     *
     * @param name Name of the configuration.
     * @param value Value of the configuration.
     */
    public void put(String name, String value) {
        configurations.put(name, value);
    }

    /**
     * Removes the configuration.
     *
     * @param name Name of the configuration.
     * @return If the configuration was removed the value of it, otherwise null.
     */
    public String remove(String name) {
        return configurations.remove(name);
    }

    /**
     * Determines if the configuration exists.
     *
     * @param name Name of the configuration.
     * @return True if the configuration exists, otherwise false.
     */
    public boolean contains(String name) {
        return configurations.containsKey(name);
    }

    /**
     * @return a clone of the Configuration object.
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Configuration clone() {
        return new Configuration(configurations);
    }

    /**
     * Attempts to convert the configuration value to {@code T}.
     *
     * If the value is null or empty then the default value is returned.
     *
     * @param value Configuration value retrieved from the map.
     * @param defaultValue Default value to return if the configuration value is null or empty.
     * @param <T> Generic type that the value is converted to if not null or empty.
     * @return The converted configuration, if null or empty the default value.
     */
    @SuppressWarnings("unchecked")
    private <T> T convertOrDefault(String value, T defaultValue) {
        // Value is null or empty, return the default.
        if (ImplUtils.isNullOrEmpty(value)) {
            return defaultValue;
        }

        // Check the default value's type to determine how it needs to be converted.
        Object convertedValue;
        if (defaultValue instanceof Byte) {
            convertedValue = Byte.parseByte(value);
        } else if (defaultValue instanceof Short) {
            convertedValue = Short.parseShort(value);
        } else if (defaultValue instanceof Integer) {
            convertedValue = Integer.parseInt(value);
        } else if (defaultValue instanceof Long) {
            convertedValue = Long.parseLong(value);
        } else if (defaultValue instanceof Float) {
            convertedValue = Float.parseFloat(value);
        } else if (defaultValue instanceof Double) {
            convertedValue = Double.parseDouble(value);
        } else if (defaultValue instanceof Boolean) {
            convertedValue = Boolean.parseBoolean(value);
        } else {
            convertedValue = value;
        }

        return (T) convertedValue;
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
    private String getOrLoad(String name) {
        if (configurations.containsKey(name)) {
            return configurations.get(name);
        }

        String value = System.getProperty(name);
        if (!ImplUtils.isNullOrEmpty(value)) {
            configurations.put(name, value);
            logger.asInformational().log("Found configuration {} in the runtime parameters.", name);
            return value;
        }

        value = System.getenv(name);
        if (!ImplUtils.isNullOrEmpty(value)) {
            configurations.put(name, value);
            logger.asInformational().log("Found configuration {} in the environment variables.", name);
            return value;
        }

        return null;
    }
}
