// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.util.configuration;

import io.clientcore.core.implementation.util.EnvironmentConfiguration;

import java.util.function.Function;

/*
 * Noop Configuration used to opt out of using global configurations when constructing client libraries.
 */
class NoopConfiguration extends Configuration {

    /**
     * Constructs a configuration containing the known properties constants. Use {@link ConfigurationBuilder} to create
     * instance of {@link Configuration}.
     *
     * @param configurationSource Configuration property source.
     * @param environmentConfiguration instance of {@link EnvironmentConfiguration} to mock environment for testing.
     * @param path Absolute path of current configuration section for logging and diagnostics purposes.
     * @param sharedConfiguration Instance of shared {@link Configuration} section to retrieve shared properties.
     */
    NoopConfiguration(ConfigurationSource configurationSource, EnvironmentConfiguration environmentConfiguration,
        String path, Configuration sharedConfiguration) {
        super(configurationSource, environmentConfiguration, path, sharedConfiguration);
    }

    @Override
    public String get(String name) {
        return null;
    }

    @Override
    public <T> T get(String name, T defaultValue) {
        return defaultValue;
    }

    @Override
    public <T> T get(String name, Function<String, T> converter) {
        return null;
    }

    @Override
    public boolean contains(String name) {
        return false;
    }
}
