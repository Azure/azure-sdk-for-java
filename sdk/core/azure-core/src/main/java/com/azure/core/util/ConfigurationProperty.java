// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents configuration property.
 *
 * @param <T> Type of property value.
 */
public class ConfigurationProperty<T> {
    private static final Function<String, Boolean> BOOLEAN_CONVERTER = (value) -> Boolean.parseBoolean(value);
    private static final Function<String, Duration> DURATION_CONVERTER = (value) -> {
        long timeoutMillis = Long.parseLong(value);
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("Duration can't be negative");
        }

        return Duration.ofMillis(timeoutMillis);
    };

    private static final Function<String, Integer> INTEGER_CONVERTER = (value) -> Integer.valueOf(value);
    private static final Function<String, String> STRING_CONVERTER = (value) -> value;
    private static final String[] EMPTY_LIST = new String[0];

    private final String name;
    private final String[] aliases;
    private final String[] environmentVariables;
    private final Function<String, T> converter;
    private final T defaultValue;
    private final boolean isShared;
    private final boolean canLogValue;
    private final boolean isRequired;

    /**
     * Creates configuration property.
     *
     * @param name full name (including relative path) of the property.
     * @param defaultValue default value.
     * @param isRequired indicates required property for validation purposes
     * @param converter converts String property value to property type and performs validation.
     * @param isShared indicates property that can be specified in per-client and root sections.
     * @param environmentVariables list of environment variables the property can be represented with. Can be null.
     * @param aliases list of alternative names of the property. Can be null.
     * @param canLogValue indicated is property value can be securely logged.
     */
    ConfigurationProperty(String name, T defaultValue, boolean isRequired, Function<String, T> converter, boolean isShared,
                          String[] environmentVariables, String[] aliases, boolean canLogValue) {
        this.name = Objects.requireNonNull(name, "'name' cannot be null");
        this.converter = Objects.requireNonNull(converter, "'converter' cannot be null");
        this.environmentVariables = environmentVariables == null ? EMPTY_LIST : environmentVariables;
        this.aliases = aliases == null ? EMPTY_LIST : aliases;
        this.defaultValue = defaultValue;
        this.isRequired = isRequired;
        this.isShared = isShared;
        this.canLogValue = canLogValue;
    }

    /**
     * Returns true if property can be shared between clients and configuration should
     * look for it in per-client and root sections.
     */
    boolean isShared() {
        return isShared;
    }

    /**
     * Returns true if property value can be securely logged.
     */
    boolean canLogValue() {
        return canLogValue;
    }

    /**
     * Returns true if property is required, used for validation purposes.
     */
    boolean isRequired() {
        return isRequired;
    }

    /**
     * Gets full property name including relative path to it.
     */
    String getName() {
        return name;
    }

    /**
     * Gets converter for property value.
     */
    Function<String, T> getConverter() {
        return converter;
    }

    /**
     * Gets property default value to be used when property is missing in the configuration.
     */
    T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets property aliases - alternative names property can have.
     */
    String[] getAliases() {
        return aliases;
    }

    /**
     * Gets name of environment variables this property can be configured with.
     */
    String[] getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Creates default {@link ConfigurationPropertyBuilder} configured to redact property value.
     *
     * <!-- src_embed com.azure.core.util.Configuration#get(ConfigurationProperty) -->
     * <!-- end com.azure.core.util.Configuration#get(ConfigurationProperty) -->
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<String> stringPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<>(name, STRING_CONVERTER);
    }

    /**
     * Creates {@link ConfigurationPropertyBuilder} configured to log property value and
     * parse value using {@link Integer#valueOf(String)}, proxying {@link NumberFormatException} exception.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationProperty#integerPropertyBuilder(String) -->
     * <!-- end com.azure.core.util.ConfigurationProperty#integerPropertyBuilder(String) -->
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<Integer> integerPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<>(name, INTEGER_CONVERTER).canLogValue(true);
    }

    /**
     * Creates {@link ConfigurationPropertyBuilder} configured to log property value and
     * parses value as long number of milliseconds, proxying  {@link NumberFormatException} exception.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationProperty#durationPropertyBuilder(String) -->
     * <!-- end com.azure.core.util.ConfigurationProperty#durationPropertyBuilder(String) -->
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<Duration> durationPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<>(name, DURATION_CONVERTER).canLogValue(true);
    }

    /**
     * Creates {@link ConfigurationPropertyBuilder} configured to log property value and
     * parse value using {@link Boolean#parseBoolean(String)}.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationProperty#booleanPropertyBuilder(String) -->
     * <!-- end com.azure.core.util.ConfigurationProperty#booleanPropertyBuilder(String) -->
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<Boolean> booleanPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<>(name, BOOLEAN_CONVERTER).canLogValue(true);
    }
}
