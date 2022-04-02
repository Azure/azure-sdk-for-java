// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.CoreUtils;

import java.time.Duration;
import java.util.function.Function;

public class ConfigurationUtils {
    public static final Function<String, Boolean> CONFIGURATION_PROPERTY_BOOLEAN_CONVERTER = (value) -> Boolean.valueOf(value);
    public static final Function<String, Duration> CONFIGURATION_PROPERTY_DURATION_CONVERTER = (value) -> {
        long timeoutMillis = Long.parseLong(value);
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("Duration can't be negative");
        }

        return Duration.ofMillis(timeoutMillis);
    };

    public static final Function<String, Integer> CONFIGURATION_PROPERTY_INTEGER_CONVERTER = (value) -> Integer.valueOf(value);
    public static final Function<String, String> CONFIGURATION_PROPERTY_STRING_CONVERTER =  Function.identity();

    /**
     * Creates default {@link ConfigurationPropertyBuilder} configured to redact property value.
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<String> stringSharedPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<>(name, CONFIGURATION_PROPERTY_STRING_CONVERTER).shared(true);
    }

    /**
     * Creates {@link ConfigurationPropertyBuilder} configured to log property value and
     * parse value using {@link Integer#valueOf(String)}, proxying {@link NumberFormatException} exception.
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<Integer> integerSharedPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<>(name, CONFIGURATION_PROPERTY_INTEGER_CONVERTER).logValue(true).shared(true);
    }

    /**
     * Creates {@link ConfigurationPropertyBuilder} configured to log property value and
     * parses value as long number of milliseconds, proxying  {@link NumberFormatException} exception.
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<Duration> durationSharedPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<>(name, CONFIGURATION_PROPERTY_DURATION_CONVERTER).logValue(true).shared(true);
    }

    /**
     * Creates {@link ConfigurationPropertyBuilder} configured to log property value and
     * parse value using {@link Boolean#valueOf(String)}.
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<Boolean> booleanSharedPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<>(name, CONFIGURATION_PROPERTY_BOOLEAN_CONVERTER).logValue(true).shared(true);
    }

    /**
     * Attempts to convert the configuration value to given primitive {@code T} using
     * corresponding {@code parse} method on this type.
     *
     * <p><b>Following types are supported:</b></p>
     * <ul>
     * <li>{@link Byte}</li>
     * <li>{@link Short}</li>
     * <li>{@link Integer}</li>
     * <li>{@link Long}</li>
     * <li>{@link Float}</li>
     * <li>{@link Double}</li>
     * <li>{@link Boolean}</li>
     * </ul>
     *
     * If the value is null or empty then the default value is returned.
     *
     * @param value Configuration value retrieved from the map.
     * @param defaultValue Default value to return if the configuration value is null or empty.
     * @param <T> Generic type that the value is converted to if not null or empty.
     * @return The converted configuration, if null or empty the default value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertToPrimitiveOrDefault(String value, T defaultValue) {
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
}
