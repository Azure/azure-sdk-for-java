package com.azure.core.configuration;

public final class EnvironmentConfigurationGetter implements ConfigurationGetter {
    @Override
    public String getConfiguration(String configurationName) {
        return System.getenv(configurationName);
    }
}
