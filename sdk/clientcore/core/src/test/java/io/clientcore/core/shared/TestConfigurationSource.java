// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.shared;

import io.clientcore.core.utils.configuration.ConfigurationSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestConfigurationSource implements ConfigurationSource {
    private final Map<String, String> testData;

    public TestConfigurationSource() {
        this.testData = new HashMap<>();
    }

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
