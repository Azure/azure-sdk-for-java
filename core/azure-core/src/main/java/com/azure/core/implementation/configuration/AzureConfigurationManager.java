package com.azure.core.implementation.configuration;

import java.util.HashMap;
import java.util.Map;

public final class AzureConfigurationManager {
    private static Map<String, AzureConfiguration> configurations = new HashMap<>();

    static {
        configurations.put(EnvironmentConfigurations.AZURE_LOG_LEVEL, AzureConfiguration.AZURE_LOG_LEVEL);
        configurations.put(EnvironmentConfigurations.AZURE_LOGGING_ENABLED, AzureConfiguration.AZURE_LOGGING_ENABLED);
        configurations.put(EnvironmentConfigurations.AZURE_TRACING_ENABLED, AzureConfiguration.AZURE_TRACING_ENABLED);
    }

    public static AzureConfiguration get(String name) {
        return configurations.get(name);
    }

    public static AzureConfiguration put(String name, AzureConfiguration configuration) {
        return configurations.put(name, configuration);
    }

    public static AzureConfiguration remove(String name) {
        return configurations.remove(name);
    }
}
