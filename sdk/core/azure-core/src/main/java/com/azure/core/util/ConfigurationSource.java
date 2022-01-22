package com.azure.core.util;

import java.util.Set;

public interface ConfigurationSource {
    Set<String> getChildKeys(String path);
    String getValue(String propertyName);
}
