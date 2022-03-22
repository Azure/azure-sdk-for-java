// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.Objects;
import java.util.function.Function;

/**
 * Builds configuration property.
 * @param <T> The property value type.
 */
public class ConfigurationPropertyBuilder<T> {
    private static final String[] EMPTY_ARRAY = new String[0];

    private final String name;
    private final Function<String, T> converter;

    private String[] aliases = EMPTY_ARRAY;
    private String[] environmentAliases = EMPTY_ARRAY;
    private T defaultValue;
    private boolean shared;
    private boolean canLogValue;
    private boolean required;

    /**
     * Constructs {@code ConfigurationPropertyBuilder} instance.
     *
     * <!-- src_embed com.azure.core.util.ConfigurationPropertyBuilder -->
     * <pre>
     * ConfigurationProperty&lt;SampleEnumProperty&gt; modeProperty =
     *     new ConfigurationPropertyBuilder&lt;&gt;&#40;&quot;mode&quot;, SampleEnumProperty::fromString&#41;
     *         .canLogValue&#40;true&#41;
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
     * @param defaultValue value to be returned by {@link Configuration#get(ConfigurationProperty)} if the property isn't found.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> defaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Sets flag indicating that property can be provided in the shared configuration section
     * in addition to client-specific configuration section.
     * Default is {@code false}, indicating that property can only be provided in local configuration.
     *
     * @param shared If set to {@code true}, {@link Configuration#get(ConfigurationProperty)} will attempt to retrieve property from
     *               local configuration and fall back to shared section, when local property is missing.
     *               Otherwise, only local configuration will be checked.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> shared(boolean shared) {
        this.shared = shared;
        return this;
    }

    /**
     * Sets flag indicating if property value can be logged.
     * Default is {@code false}, indicating that property value will be redacted in logs.
     *
     * @param canLogValue If set to {@code true}, {@link Configuration#get(ConfigurationProperty)} will log property value,
     *                    Otherwise, value is redacted.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> canLogValue(boolean canLogValue) {
        this.canLogValue = canLogValue;
        return this;
    }

    /**
     * Sets flag indicating if property is required.
     * Default is {@code false}, indicating that property is optional.
     *
     * @param required If set to {@code true}, {@link Configuration#get(ConfigurationProperty)} will throw when property is not found.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> required(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Sets one or more alias for property. {@link Configuration#get(ConfigurationProperty)} attempts to retrieve property by name first
     * and only then tries to retrieve properties by alias in the order aliases are provided.
     *
     * @param aliases one or more alias for the property name.
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> aliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    /**
     * Sets one or more environment variable (or system property) property name.
     * {@link Configuration#get(ConfigurationProperty)} falls back when property value is not found by {@code name}, alias,
     * in local and shared configuration (if enabled) and only then checks environment variables (and system properties).
     * When environment properties are not set, {@link Configuration#get(ConfigurationProperty)} does not attempt to
     * read environment configuration.
     *
     * @param environmentAliases one or more environment variable (or system property).
     * @return the updated ConfigurationPropertyBuilder object.
     */
    public ConfigurationPropertyBuilder<T> environmentAliases(String... environmentAliases) {
        this.environmentAliases = environmentAliases;
        return this;
    }

    /**
     * Builds configuration property instance.
     * @return {@link ConfigurationProperty} instance.
     */
    public ConfigurationProperty<T> build() {
        return new ConfigurationProperty<>(name, defaultValue, required, converter, shared, environmentAliases, aliases, canLogValue);
    }
}
