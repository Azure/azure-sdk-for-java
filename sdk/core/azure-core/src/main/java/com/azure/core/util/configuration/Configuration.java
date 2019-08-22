// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.configuration;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.implementation.util.ImplUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Contains configuration information that is used during construction of client libraries.
 */
public class Configuration implements Cloneable {
    /**
     * Noop Configuration object used to opt out of using global configurations when constructing client libraries.
     */
    public static final Configuration NONE = new NoopConfiguration();

    private static final String LOADED_FROM_RUNTIME = "Loaded {} from runtime parameters with value {}.";
    private static final String LOADED_FROM_ENVIRONMENT = "Loaded {} from environment variables with value {}.";

    private final ClientLogger logger = new ClientLogger(Configuration.class);

    private ConcurrentMap<String, String> configurations = new ConcurrentHashMap<>();
    private boolean loadedBaseConfigurations = false;

    /**
     * Constructs an empty configuration.
     */
    public Configuration() {
    }

    private Configuration(ConcurrentMap<String, String> configurations) {
        this.configurations = new ConcurrentHashMap<>(configurations);
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
        String value = getOrLoad(name);
        if (ImplUtils.isNullOrEmpty(value)) {
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
        loadBaseConfigurations();

        // Special handling for tracing disabled and log level as they need to be updated instantly on
        // configuration change.
        if (BaseConfigurations.AZURE_TRACING_DISABLED.equalsIgnoreCase(name)
            || BaseConfigurations.AZURE_LOG_LEVEL.equalsIgnoreCase(name)) {
            load(name);
        }

        if (configurations.containsKey(name)) {
            return configurations.get(name);
        }

        return load(name);
    }

    /*
     * Attempts to load the configuration from the environment.
     *
     * The runtime parameters are checked first followed by the environment variables. If the configuration is found
     * the value is loaded into the configuration store and if a configuration with the same name already exists this
     * will update it to the loaded value.
     *
     * @param name Name of the configuration.
     * @return If found the loaded configuration, otherwise null.
     */
    private String load(String name) {
        if (loadFrom(name, System::getProperty, LOADED_FROM_RUNTIME)
            || loadFrom(name, System::getenv, LOADED_FROM_ENVIRONMENT)) {
            return configurations.get(name);
        }

        return null;
    }

    /**
     * Adds a configuration with the given value.
     *
     * If a configuration with the same name already exists this will update it to the passed value.
     *
     * @param name Name of the configuration.
     * @param value Value of the configuration.
     * @return the updated Configuration object.
     */
    public Configuration put(String name, String value) {
        configurations.put(name, value);
        return this;
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
        loadBaseConfigurations();
        Configuration clone = new Configuration(configurations);
        clone.loadedBaseConfigurations = true;

        return clone;
    }

    /*
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

    /*
     * Attempts to load the configuration using the passed loader. If the configuration is found it will be added to
     * the configuration store and a message will be logged.
     *
     * @param name Name of the configuration.
     * @param loader Loading function to apply.
     * @return True if the configuration was loaded, false otherwise.
     */
    private boolean loadFrom(String name, Function<String, String> loader, String logMessage) {
        String value = loader.apply(name);

        if (value == null) {
            // Nothing was loaded
            return false;
        } else if (value.equals(configurations.get(name))) {
            // Value loaded is the same, no need to log anything.
            return true;
        } else {
            // Value changed, log it!
            configurations.put(name, value);
            logger.info(logMessage, name, value);
            return true;
        }
    }

    /*
     * Loads all configurations in BaseConfigurations if they haven't been loaded already.
     */
    private void loadBaseConfigurations() {
        if (loadedBaseConfigurations) {
            return;
        }

        for (String config : BaseConfigurations.DEFAULT_CONFIGURATIONS) {
            if (!configurations.containsKey(config)) {
                load(config);
            }
        }

        loadedBaseConfigurations = true;
    }
}
