// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils.configuration;

import java.util.Objects;

/**
 * Implementation of {@link Configuration} that reads configuration values from {@link System#getenv(String)}.
 */
public final class EnvironmentVariableConfigurationSource implements ConfigurationSource {
    /**
     * Creates an instance of {@link EnvironmentVariableConfigurationSource}.
     */
    public EnvironmentVariableConfigurationSource() {
    }

    @Override
    public String getProperty(String name) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        return System.getenv(name);
    }

    @Override
    public boolean isMutable() {
        return false;
    }
}
