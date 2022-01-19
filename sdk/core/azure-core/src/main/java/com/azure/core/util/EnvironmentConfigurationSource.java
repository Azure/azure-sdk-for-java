package com.azure.core.util;

import java.util.Collections;

class EnvironmentConfigurationSource implements ConfigurationSource {

    @Override
    public Iterable<String> getValues(String prefix) {
        // does not support prefixes, we should never be here
        return Collections.emptyList();
    }

    @Override
    public String getValue(String propertyName) {
        String value = System.getProperty(propertyName);
        if (value == null) {
            value = System.getenv(propertyName);
        }

        return value;
    }
}
