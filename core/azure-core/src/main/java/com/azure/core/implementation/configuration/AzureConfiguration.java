// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

import com.azure.core.implementation.logging.ServiceLoggerAPI;

import java.util.function.Function;

public final class AzureConfiguration<T> {
    public static AzureConfiguration<Integer> azureLoggingLevel = new AzureConfiguration<>(EnvironmentConfigurations.AZURE_LOG_LEVEL, Integer::parseInt, ServiceLoggerAPI.DISABLED_LEVEL);
    public static AzureConfiguration<Boolean> azureLoggingEnabled = new AzureConfiguration<>(EnvironmentConfigurations.AZURE_TELEMETRY_DISABLED, Boolean::parseBoolean, false);
    public static AzureConfiguration<Boolean> azureTracingEnabled = new AzureConfiguration<>(EnvironmentConfigurations.AZURE_TRACING_DISABLED, Boolean::parseBoolean, true);

    @SuppressWarnings("unchecked")
    private final Function<String, T> defaultConverter = (value) -> (T) value;

    private EnvironmentConfigurations environmentConfigurations;
    private Function<String, T> converter;
    private T globalDefaultValue;

    public AzureConfiguration(EnvironmentConfigurations environmentConfigurations) {
        this.environmentConfigurations = environmentConfigurations;
        this.converter = defaultConverter;
        this.globalDefaultValue = null;
    }

    public AzureConfiguration(EnvironmentConfigurations environmentConfigurations, Function<String, T> converter, T defaultValue) {
        this.environmentConfigurations = environmentConfigurations;
        this.converter = converter;
        this.globalDefaultValue = defaultValue;
    }

    public T getConfiguration() {
        T configuration = getConfigurationFromManager();
        return (configuration == null) ? globalDefaultValue : configuration;
    }

    public T getConfiguration(T defaultValue) {
        T configuration = getConfiguration();
        return (configuration == null) ? defaultValue : configuration;
    }

    private T getConfigurationFromManager() {
        return converter.apply(ConfigurationManager.getConfiguration(environmentConfigurations));
    }
}
