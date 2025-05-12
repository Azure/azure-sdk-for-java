// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.util;

import io.clientcore.core.utils.configuration.ConfigurationSource;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link ConfigurationSource} that contains empty values for AZURE_* environment variables.
 */
public class TestConfigurationSource implements ConfigurationSource {
    private final Map<String, String> testData;

    public TestConfigurationSource() {
        this.testData = new HashMap<>();
    }

    @Override
    public String getProperty(String name) {
        return testData.get(name);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    public TestConfigurationSource put(String name, String value) {
        testData.put(name, value);
        return this;
    }
}
