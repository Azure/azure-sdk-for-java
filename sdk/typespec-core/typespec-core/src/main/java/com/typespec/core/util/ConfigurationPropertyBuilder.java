// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * Builds configuration property.
 *
 * @param <T> The property value type.
 */
public final class ConfigurationPropertyBuilder<T> {
    private static final String[] EMPTY_ARRAY = new String[0];
    private static final Function<String, String> PERMIT_VALUE_SANITIZER = (value) -> value;
    private static final Function<String, Boolean> CONFIGURATION_PROPERTY_BOOLEAN_CONVERTER = Boolean::valueOf;
    private static final Function<String, Duration> CONFIGURATION_PROPERTY_DURATION_CONVERTER = (value) -> {
        long timeoutMillis = Long.parseLong(value);
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("Duration can't be negative");
        }

        return Duration.ofMillis(timeoutMillis);
    };

    private static final Function<String, Integer> CONFIGURATION_PROPERTY_INTEGER_CONVERTER = Integer::valueOf;
    private static final Function<String, String> CONFIGURATION_PROPERTY_STRING_CONVERTER = Function.identity();

    private final String name;
    private final Function<String, T> converter;

    private String[] aliases = EMPTY_ARRAY;

    private String environmentVariableName;
    private String systemPropertyName;
    private T defaultValue;
    private boolean shared;
    private Function<String, String> valueSanitizer;
    private boolean required;

    /**
     * Creates default {@link ConfigurationPropertyBuilder}. String property values are redacted in logs by default. If
     * property value does not contain sensitive information, use {@link ConfigurationPropertyBuilder#logValue} to
     * enable logging.
     *
     * <!-- src_embed com.azure.core.util.Configuration.get#ConfigurationProperty -->
     * <pre>
     * ConfigurationProperty&lt;String&gt; property = ConfigurationPropertyBuilder.ofString&#40;&quot;http.proxy.hostname&quot;&#41;
     *     .shared&#40;true&#41;
     *     .logValue&#40;true&#41;
     *     .systemPropertyName&#40;&quot;http.proxyHost&quot;&#41;
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
    public static ConfigurationPropertyBuilder<String> ofString(String name) {
        return new ConfigurationPropertyBuilder<>(name, CONFIGURATION_PROPERTY_STRING_CONVERTER);
    }

    /**
     * Creates {@link ConfigurationPropertyBuilder} configured to log property value and parse value using
     * {@link Integer#valueOf(String)}, proxying {@link NumberFormatException} exception.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationPropertyBuilder.ofInteger -->
     * <pre>
     * ConfigurationProperty&lt;Integer&gt; integerProperty = ConfigurationPropertyBuilder.ofInteger&#40;&quot;retry-count&quot;&#41;
     *     .build&#40;&#41;;
     * System.out.println&#40;configuration.get&#40;integerProperty&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.ConfigurationPropertyBuilder.ofInteger -->
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<Integer> ofInteger(String name) {
        return new ConfigurationPropertyBuilder<>(name, CONFIGURATION_PROPERTY_INTEGER_CONVERTER).logValue(true);
    }

    /**
     * Creates {@link ConfigurationPropertyBuilder} configured to log property value and parses value as long number of
     * milliseconds, proxying  {@link NumberFormatException} exception.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationPropertyBuilder.ofDuration -->
     * <pre>
     * ConfigurationProperty&lt;Duration&gt; timeoutProperty = ConfigurationPropertyBuilder.ofDuration&#40;&quot;timeout&quot;&#41;
     *     .build&#40;&#41;;
     * System.out.println&#40;configuration.get&#40;timeoutProperty&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.ConfigurationPropertyBuilder.ofDuration -->
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<Duration> ofDuration(String name) {
        return new ConfigurationPropertyBuilder<>(name, CONFIGURATION_PROPERTY_DURATION_CONVERTER).logValue(true);
    }

    /**
     * Creates {@link ConfigurationPropertyBuilder} configured to log property value and parse value using
     * {@link Boolean#parseBoolean(String)}.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationPropertyBuilder.ofBoolean -->
     * <pre>
     * ConfigurationProperty&lt;Boolean&gt; booleanProperty = ConfigurationPropertyBuilder.ofBoolean&#40;&quot;is-enabled&quot;&#41;
     *     .build&#40;&#41;;
     * System.out.println&#40;configuration.get&#40;booleanProperty&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.ConfigurationPropertyBuilder.ofBoolean -->
     *
     * @param name property name.
     * @return instance of {@link ConfigurationPropertyBuilder}.
     */
    public static ConfigurationPropertyBuilder<Boolean> ofBoolean(String name) {
        return new ConfigurationPropertyBuilder<>(name, CONFIGURATION_PROPERTY_BOOLEAN_CONVERTER).logValue(true);
    }

    /**
     * Constructs {@code ConfigurationPropertyBuilder} instance.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationPropertyBuilder -->
     * <pre>
     * ConfigurationProperty&lt;SampleEnumProperty&gt; modeProperty =
     *     new ConfigurationPropertyBuilder&lt;&gt;&#40;&quot;mode&quot;, SampleEnumProperty::fromString&#41;
     *         .logValue&#40;true&#41;
     *         .defaultValue&#40;SampleEnumProperty.MODE_1&#41;
     *         .build&#40;&#41;;
     * System.out.println&#40;configuration.get&#40;modeProperty&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.ConfigurationPropertyBuilder -->
     *
     * @param name name of the property.
     * @param converter Converter used to map the configuration to {@code T}.
     */
    public ConfigurationPropertyBuilder(String name, Function<String, T> converter) {
        this.name = Objects.requireNonNull(name, "'name' cannot be null");
        this.converter = Objects.requireNonNull(converter, "'converter' cannot be null");
    }

    /**
     * Sets default property value. {@code null} by default.
     *
     * @param defaultValue value to be returned by {@link Configuration#get(ConfigurationProperty)} if the property
     * isn't found.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> defaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Sets flag indicating that property can be provided in the shared configuration section in addition to
     * client-specific configuration section. Default is {@code false}, indicating that property can only be provided in
     * local configuration.
     *
     * @param shared If set to {@code true}, {@link Configuration#get(ConfigurationProperty)} will attempt to retrieve
     * property from local configuration and fall back to shared section, when local property is missing. Otherwise,
     * only local configuration will be checked.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> shared(boolean shared) {
        this.shared = shared;
        return this;
    }

    /**
     * Sets flag indicating if property value can be logged. Default is {@code false}, indicating that property value
     * should not be logged. When and if retrieving of corresponding configuration property is logged,
     * {@link Configuration} will use "redacted" string instead of property value. If flag is set to {@code true}, value
     * is populated on the logs as is.
     *
     * @param logValue If set to {@code true}, {@link Configuration#get(ConfigurationProperty)} will log property value,
     * Otherwise, value is redacted.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> logValue(boolean logValue) {
        if (logValue) {
            this.valueSanitizer = PERMIT_VALUE_SANITIZER;
        }

        return this;
    }

    /**
     * Sets flag indicating if property is required. Default is {@code false}, indicating that property is optional.
     *
     * @param required If set to {@code true}, {@link Configuration#get(ConfigurationProperty)} will throw when property
     * is not found.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> required(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Sets one or more alias for property. {@link Configuration#get(ConfigurationProperty)} attempts to retrieve
     * property by name first and only then tries to retrieve properties by alias in the order aliases are provided.
     *
     * @param aliases one or more alias for the property name.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> aliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    /**
     * Sets environment variable name that can represent this property if explicit configuration is not set.
     *
     * <p>
     * When property value is not found by {@code name} or {@code alias},
     * {@link Configuration#get(ConfigurationProperty)} falls back to system properties and environment variables.
     * <p>
     * When environment variable (or system property) is not set, {@link Configuration#get(ConfigurationProperty)} does
     * not attempt to read environment configuration.
     *
     * @param environmentVariableName environment variable name.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> environmentVariableName(String environmentVariableName) {
        this.environmentVariableName = environmentVariableName;
        return this;
    }

    /**
     * Sets system property name that can represent this property if explicit configuration is not set.
     *
     * <p>
     * When property value is not found by {@code name} or {@code alias},
     * {@link Configuration#get(ConfigurationProperty)} falls back to system properties and environment variables.
     * <p>
     * When environment variable (or system property) is not set, {@link Configuration#get(ConfigurationProperty)} does
     * not attempt to read environment configuration.
     *
     * @param systemPropertyName one or more environment variable (or system property).
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> systemPropertyName(String systemPropertyName) {
        this.systemPropertyName = systemPropertyName;
        return this;
    }

    /**
     * Builds configuration property instance.
     *
     * @return {@link ConfigurationProperty} instance.
     */
    public ConfigurationProperty<T> build() {
        return new ConfigurationProperty<>(name, defaultValue, required, converter, shared, environmentVariableName,
            systemPropertyName, aliases, valueSanitizer);
    }
}
