// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.util.ConfigurationSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test configuration source implementation.
 */
public final class TestConfigurationSource implements ConfigurationSource {
    private final Map<String, String> testData;

    public TestConfigurationSource(String... testData) {
        this.testData = new HashMap<>();

        if (testData != null) {
            if (testData.length % 2 != 0) {
                throw new IllegalArgumentException("Configuration 'testData' length is odd, expected to be even to represent names and values of properties");
            }
            for (int i = 0; i < testData.length; i += 2) {
                this.testData.put(testData[i], testData[i + 1]);
            }
        }
    }

    public TestConfigurationSource add(String key, String value) {
        this.testData.put(key, value);
        return this;
    }

    @Override
    public Map<String, String> getProperties(String path) {
        if (path == null) {
            return testData;
        }
        return testData.entrySet().stream()
            .filter(prop -> prop.getKey().startsWith(path + "."))
            .collect(Collectors.toMap(Map.Entry<String, String>::getKey, Map.Entry<String, String>::getValue));
    }
}
