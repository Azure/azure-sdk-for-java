// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils;

import com.typespec.core.util.ConfigurationSource;

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
     */
    public TestConfigurationSource() {
        this.testData = new HashMap<>();
    }

    /**
     * Adds property name and value to the source.
     *
     * @param name property name
     * @param value property value
     * @return this {@code TestConfigurationSource} for chaining.
     */
    public TestConfigurationSource put(String name, String value) {
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
