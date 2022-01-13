package com.azure.core.util;

public interface ConfigurationSource {
    String getValue(String propertyName);
}
