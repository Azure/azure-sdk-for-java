package com.azure.core.util;

public interface ConfigurationSource {
    Iterable<String> getValues(String prefix);
    String getValue(String propertyName);
}
