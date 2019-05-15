// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

import com.azure.core.implementation.logging.ServiceLoggerAPI;

import java.util.function.Function;

public final class AzureConfiguration {
    public static final AzureConfiguration AZURE_LOG_LEVEL = new AzureConfiguration(EnvironmentConfigurations.AZURE_LOG_LEVEL, Integer::parseInt, ServiceLoggerAPI.DISABLED_LEVEL);
    public static final AzureConfiguration AZURE_LOGGING_ENABLED = new AzureConfiguration(EnvironmentConfigurations.AZURE_LOGGING_ENABLED, Boolean::parseBoolean, false);
    public static final AzureConfiguration AZURE_TRACING_ENABLED = new AzureConfiguration(EnvironmentConfigurations.AZURE_TRACING_ENABLED, Boolean::parseBoolean, true);

    @SuppressWarnings("unchecked")
    private static final Function<String, Object> DEFAULT_CONVERTER = (value) -> (String) value;

    private String configuration;
    private Function<String, Object> converter;
    private Object defaultValue;

    public AzureConfiguration(EnvironmentConfigurations environmentConfigurations) {
        this.configuration = environmentConfigurations.toString();
        this.converter = DEFAULT_CONVERTER;
        this.defaultValue = null;
    }

    public AzureConfiguration(String configuration, Function<String, Object> converter, Object defaultValue) {
        this.configuration = configuration;
        this.converter = converter;
        this.defaultValue = defaultValue;
    }

    public Object getConfiguration() {
        Object configuration = findConfiguration();
        return (configuration == null) ? defaultValue : configuration;
    }

    public Object getConfiguration(Object defaultValue) {
        if (defaultValue != null) {
            return defaultValue;
        }

        return getConfiguration();
    }

    private Object findConfiguration() {
        return converter.apply(AzureConfigurationRetriever.retrieve(configuration));
    }
}
