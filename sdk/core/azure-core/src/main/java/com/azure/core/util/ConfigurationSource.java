package com.azure.core.util;

public interface ConfigurationSource {
    Iterable<String> getChildKeys(String path);
    String getValue(String propertyName);
}
