// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TestConfigurationSource implements ConfigurationSource {
    private Map<String, String> testData;

    public TestConfigurationSource() {
        this.testData = new HashMap<>();
    }

    public TestConfigurationSource put(String key, String value) {
        this.testData.put(key, value);
        return this;
    }

    @Override
    public Map<String, String> getProperties(String path) {
        if (path == null) {
            return testData;
        }
        return testData.entrySet()
            .stream()
            .filter(prop -> prop.getKey().startsWith(path + "."))
            .collect(Collectors.toMap(Map.Entry<String, String>::getKey, Map.Entry<String, String>::getValue));
    }
}
