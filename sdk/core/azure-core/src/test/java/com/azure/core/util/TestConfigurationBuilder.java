// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

public class TestConfigurationBuilder {
    private TestConfigurationSource source;
    private TestConfigurationSource envConfig;

    public TestConfigurationBuilder(String... testProps) {
        this.source = new TestConfigurationSource(testProps);
        this.envConfig = new TestConfigurationSource();
    }

    public TestConfigurationBuilder add(String key, String value) {
        source.add(key, value);
        return this;
    }

    public TestConfigurationBuilder addEnv(String key, String value) {
        envConfig.add(key, value);
        return this;
    }

    public Configuration build() {
        return new ConfigurationBuilder(source, envConfig).build();
    }

    public Configuration buildSection(String section) {
        return new ConfigurationBuilder(source, envConfig).buildSection(section);
    }
}
