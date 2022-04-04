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

    /**
     * Creates TestConfigurationSource with given property names and values.
     *
     * @param testProperties array of interleaved key-value pairs.
     * @throws IllegalArgumentException if test properties' length is odd.
     */
    public TestConfigurationSource(String... testProperties) {
        this.testData = new HashMap<>();

        if (testProperties != null) {
            if (testProperties.length % 2 != 0) {
                throw new IllegalArgumentException("Configuration 'testData' length is odd, expected to be even to represent names and values of properties");
            }
            for (int i = 0; i < testProperties.length; i += 2) {
                this.testData.put(testProperties[i], testProperties[i + 1]);
            }
        }
    }

    /**
     * Adds property name and value to the source.
     *
     * @param name property name
     * @param value property value
     * @return this {@code TestConfigurationSource} for chaining.
     */
    public TestConfigurationSource add(String name, String value) {
        this.testData.put(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
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
