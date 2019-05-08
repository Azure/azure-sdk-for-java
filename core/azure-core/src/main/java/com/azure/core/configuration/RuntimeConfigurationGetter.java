package com.azure.core.configuration;

public final class RuntimeConfigurationGetter implements ConfigurationGetter {
    @Override
    public String getConfiguration(String configurationName) {
        return System.getProperty(configurationName);
    }
}
