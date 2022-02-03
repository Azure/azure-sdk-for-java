package com.azure.core.implementation.util;

import com.azure.core.util.ConfigurationSource;
import com.azure.core.util.CoreUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Contains configuration information that is used during construction of client libraries.
 */
public class EnvironmentConfiguration  {
    public static final EnvironmentConfigurationSource ENVIRONMENT_SOURCE = new EnvironmentConfigurationSource();
    private static final EnvironmentConfiguration GLOBAL_CONFIGURATION = new EnvironmentConfiguration(ENVIRONMENT_SOURCE);

    private final ConcurrentMap<String, String> configurations;
    private final ConfigurationSource source;

    /**
     * Constructs a configuration containing the known Azure properties constants.
     */
    public EnvironmentConfiguration(ConfigurationSource source) {
        this.source = source;
        this.configurations = loadBaseConfiguration(source);
    }

    private EnvironmentConfiguration(EnvironmentConfiguration original) {
        this.source = original.source;
        this.configurations = new ConcurrentHashMap<>(original.configurations);
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
        return convertOrDefault(getOrLoad(name), defaultValue);
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
        String value = getOrLoad(name);
        if (CoreUtils.isNullOrEmpty(value)) {
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
    String getOrLoad(String name) {
        String value = configurations.get(name);
        if (value != null) {
            return value;
        }

        value = source.getValue(name);
        if (value != null) {
            configurations.put(name, value);
            return value;
        }

        return null;
    }

    /**
     * Adds a configuration with the given value.
     * <p>
     * This will overwrite the previous configuration value if it existed.
     *
     * @param name Name of the configuration.
     * @param value Value of the configuration.
     * @return The updated Configuration object.
     */
    @Deprecated
    public EnvironmentConfiguration put(String name, String value) {
        configurations.put(name, value);
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
    @Deprecated
    public String remove(String name) {
        return configurations.remove(name);
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
        return configurations.containsKey(name);
    }

    /**
     * Clones this Configuration object.
     *
     * @return A clone of the Configuration object.
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public EnvironmentConfiguration clone() {
        return new EnvironmentConfiguration(this);
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
    public static <T> T convertOrDefault(String value, T defaultValue) {
        // TODO to helper
        // Value is null or empty, return the default.
        if (CoreUtils.isNullOrEmpty(value)) {
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

    private static ConcurrentMap<String, String> loadBaseConfiguration(ConfigurationSource source) {
        ConcurrentMap<String, String> configurations = new ConcurrentHashMap<>();
        for (String config : source.getChildKeys(null)) {
            String value = source.getValue(config);
            if (value != null) {
                configurations.put(config, value);
            }
        }

        return configurations;
    }
}
