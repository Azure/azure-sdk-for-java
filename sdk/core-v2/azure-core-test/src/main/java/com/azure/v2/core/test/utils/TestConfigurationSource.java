// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils;

import io.clientcore.core.utils.configuration.ConfigurationSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Test configuration source implementation.
 */
public class TestConfigurationSource implements ConfigurationSource {
    private final Map<String, String> testData;

    /**
     * Creates an instance of {@link TestConfigurationSource}.
     */
    public TestConfigurationSource() {
        this.testData = new HashMap<>();
    }

    /**
     * Adds a key-value pair to the test configuration source.
     *
     * @param key The key to add.
     * @param value The value to add.
     * @return The current instance of {@link TestConfigurationSource}.
     */
    public TestConfigurationSource put(String key, String value) {
        this.testData.put(key, value);
        return this;
    }

    @Override
    public String getProperty(String name) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        return testData.get(name);
    }

    @Override
    public boolean isMutable() {
        return true;
    }
}
