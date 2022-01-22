package com.azure.core.util;

public class TestConfigurationBuilder {
    private TestConfigurationSource source;
    private TestConfigurationSource envSource;

    public TestConfigurationBuilder(String... testProps) {
        source = new TestConfigurationSource(testProps);
        envSource = new TestConfigurationSource();
    }

    public TestConfigurationBuilder setEnv(String... testEnv) {
        envSource = new TestConfigurationSource(testEnv);
        return this;
    }

    public Configuration build() {
        return new ConfigurationBuilder(source, envSource).build();
    }

}
