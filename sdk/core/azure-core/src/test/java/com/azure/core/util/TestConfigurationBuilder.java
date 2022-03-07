// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.util.EnvironmentConfiguration;

public class TestConfigurationBuilder {
    private TestConfigurationSource source;
    private EnvironmentConfiguration envConfig;

    public TestConfigurationBuilder(String... testProps) {
        this.source = new TestConfigurationSource(testProps);
        this.envConfig = new EnvironmentConfiguration();
    }

    public TestConfigurationBuilder add(String key, String value) {
        source.add(key, value);
        return this;
    }

    public TestConfigurationBuilder addEnv(String key, String value) {
        envConfig.put(key, value);
        return this;
    }

    public Configuration build() {
        return new ConfigurationBuilder(source, envConfig).build();
    }

    public Configuration buildSection(String section) {
        return new ConfigurationBuilder(source, envConfig).buildSection(section);
    }
}
