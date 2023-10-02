// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


/**
 * Represents configuration property.
 *
 * @param <T> Type of property value.
 */
public final class ConfigurationProperty<T> {
    private static final Function<String, String> REDACT_VALUE_SANITIZER = (value) -> "redacted";

    private final String name;
    private final List<String> aliases;
    private final String environmentVariable;
    private final String systemProperty;
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
     * @param environmentVariable environment variables the property can be represented with. Can be null.
     * @param systemProperty system property the property can be represented with. Can be null.
     * @param aliases list of alternative names of the property. Can be null.
     * @param valueSanitizer sanitizes property value for logging purposes.
     */
    ConfigurationProperty(String name, T defaultValue, boolean isRequired, Function<String, T> converter,
        boolean isShared, String environmentVariable, String systemProperty, String[] aliases,
        Function<String, String> valueSanitizer) {
        this.name = Objects.requireNonNull(name, "'name' cannot be null");
        this.converter = Objects.requireNonNull(converter, "'converter' cannot be null");
        this.environmentVariable = environmentVariable;
        this.systemProperty = systemProperty;
        this.aliases = aliases == null ? Collections.emptyList() : Arrays.asList(aliases);
        this.defaultValue = defaultValue;
        this.isRequired = isRequired;
        this.isShared = isShared;
        this.valueSanitizer = valueSanitizer == null ? REDACT_VALUE_SANITIZER : valueSanitizer;
    }

    /**
     * Returns true if property can be shared between clients and {@link Configuration#get(ConfigurationProperty)}
     * should look for it in per-client and root sections.
     *
     * @return flag indicating if the property is shared.
     */
    public boolean isShared() {
        return isShared;
    }

    /**
     * Returns property value sanitizer that is used to securely log property value.
     *
     * @return function that sanitizes property value.
     */
    public Function<String, String> getValueSanitizer() {
        return valueSanitizer;
    }

    /**
     * Returns true if property is required, used for validation purposes.
     *
     * @return flag indicating if the property is required.
     */
    public boolean isRequired() {
        return isRequired;
    }

    /**
     * Gets full property name including relative path to it.
     *
     * @return property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets converter for property value.
     *
     * @return property value converter.
     */
    public Function<String, T> getConverter() {
        return converter;
    }

    /**
     * Gets property default value to be used when property is missing in the configuration.
     *
     * @return default value.
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets property aliases - alternative names property can have.
     *
     * @return array of name aliases.
     */
    public Iterable<String> getAliases() {
        return aliases;
    }

    /**
     * Gets name of environment variables this property can be configured with.
     *
     * @return environment variable name.
     */
    public String getEnvironmentVariableName() {
        return environmentVariable;
    }

    /**
     * Gets name of system property this property can be configured with.
     *
     * @return system property name.
     */
    public String getSystemPropertyName() {
        return systemProperty;
    }
}
