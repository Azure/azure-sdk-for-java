package com.azure.core.util;

import java.util.Map;

public interface ConfigurationSource {
    String getValue(String propertyName);

    public static final ConfigurationSource ENV_VAR_SOURCE = new ConfigurationSource() {
        @Override
        public String getValue(String propertyName) {
            return System.getenv(propertyName);
        }
    };

    public static final ConfigurationSource SYSTEM_PROP_SOURCE = new ConfigurationSource() {
        @Override
        public String getValue(String propertyName) {
            return System.getProperty(propertyName);
        }
    };

    // TODO(configuration) should we ship one by default right away or leave it to users?
    public static class FileSource implements ConfigurationSource {
        public final Map<String, String> properties;
        public FileSource(String fileName) {
            properties = CoreUtils.getProperties(fileName);
        }

        @Override
        public String getValue(String propertyName) {
            return properties.get(propertyName);
        }
    }
}
