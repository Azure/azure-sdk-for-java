// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test implementation of {@link ConfigurationSource} to mock properties.
 */
public class TestConfigurationSource implements ConfigurationSource {
    private final Map<String, String> testData;

    /**
     * Creates {@code TestConfigurationSource}.
     */
    public TestConfigurationSource() {
        this.testData = new HashMap<>();
    }

    /**
     * Adds configuration property to the source.
     *
     * @param name property name.
     * @param value property value.
     * @return updated {@link TestConfigurationSource} for chaining.
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

    /**
     * Builds configuration from this source.
     *
     * @return {@link Configuration} instance.
     */
    public Configuration getConfiguration() {
        return new ConfigurationBuilder(this).build();
    }
}
