// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

public class EnvironmentConfigurationProvider {
    private final EnvironmentConfiguration environmentConfiguration;

    protected EnvironmentConfigurationProvider(EnvironmentConfiguration configuration) {
        this.environmentConfiguration = configuration;
    }

    public EnvironmentConfiguration getEnvironment() {
        return environmentConfiguration;
    }
}
