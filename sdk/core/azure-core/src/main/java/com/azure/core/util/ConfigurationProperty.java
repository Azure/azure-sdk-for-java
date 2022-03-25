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
public final class ConfigurationProperty<T> {
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
    private static final String[] EMPTY_ARRAY = new String[0];
    private static final Function<String, String> REDACT_VALUE_SANITIZER = (value) -> "redacted";

    private final String name;
    private final String[] aliases;
    private final String[] environmentVariables;
    private final Function<String, T> converter;
    private final Function<String, String> valueSanitizer;
    private final T defaultValue;
    private final boolean isShared;
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
     * @param valueSanitizer sanitizes property value for logging purposes.
     */
    ConfigurationProperty(String name, T defaultValue, boolean isRequired, Function<String, T> converter, boolean isShared,
                          String[] environmentVariables, String[] aliases, Function<String, String> valueSanitizer) {
        this.name = Objects.requireNonNull(name, "'name' cannot be null");
        this.converter = Objects.requireNonNull(converter, "'converter' cannot be null");
        this.environmentVariables = environmentVariables == null ? EMPTY_ARRAY : environmentVariables;
        this.aliases = aliases == null ? EMPTY_ARRAY : aliases;
        this.defaultValue = defaultValue;
        this.isRequired = isRequired;
        this.isShared = isShared;
        this.valueSanitizer = valueSanitizer == null ? REDACT_VALUE_SANITIZER : valueSanitizer;
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
    Function<String, String> getValueSanitizer() {
        return valueSanitizer;
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
     * <!-- src_embed com.azure.core.util.Configuration.get#ConfigurationProperty -->
     * <pre>
     * ConfigurationProperty&lt;String&gt; property = ConfigurationProperty.stringPropertyBuilder&#40;&quot;http.proxy.host&quot;&#41;
     *     .shared&#40;true&#41;
     *     .canLogValue&#40;true&#41;
     *     .environmentAliases&#40;&quot;http.proxyHost&quot;&#41;
     *     .build&#40;&#41;;
     *
     * &#47;&#47; attempts to get local `azure.sdk.&lt;client-name&gt;.http.proxy.host` property and falls back to
     * &#47;&#47; shared azure.sdk.http.proxy.port
     * System.out.println&#40;configuration.get&#40;property&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.Configuration.get#ConfigurationProperty -->
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
     * <!-- src_embed com.azure.core.util.ConfigurationProperty.integerPropertyBuilder -->
     * <pre>
     * ConfigurationProperty&lt;Integer&gt; integerProperty = ConfigurationProperty.integerPropertyBuilder&#40;&quot;retry-count&quot;&#41;
     *     .build&#40;&#41;;
     * System.out.println&#40;configuration.get&#40;integerProperty&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.ConfigurationProperty.integerPropertyBuilder -->
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
     * <!-- src_embed com.azure.core.util.ConfigurationProperty.durationPropertyBuilder -->
     * <pre>
     * ConfigurationProperty&lt;Duration&gt; timeoutProperty = ConfigurationProperty.durationPropertyBuilder&#40;&quot;timeout&quot;&#41;
     *     .build&#40;&#41;;
     * System.out.println&#40;configuration.get&#40;timeoutProperty&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.ConfigurationProperty.durationPropertyBuilder -->
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
     * <!-- src_embed com.azure.core.util.ConfigurationProperty.booleanPropertyBuilder -->
     * <pre>
     * ConfigurationProperty&lt;Boolean&gt; booleanProperty = ConfigurationProperty.booleanPropertyBuilder&#40;&quot;is-enabled&quot;&#41;
     *     .build&#40;&#41;;
     * System.out.println&#40;configuration.get&#40;booleanProperty&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.ConfigurationProperty.booleanPropertyBuilder -->
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<Boolean> booleanPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<>(name, BOOLEAN_CONVERTER).canLogValue(true);
    }
}
