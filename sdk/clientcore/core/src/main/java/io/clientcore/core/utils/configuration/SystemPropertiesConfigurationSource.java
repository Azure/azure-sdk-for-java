// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils.configuration;

import java.util.Objects;

/**
 * Implementation of {@link Configuration} that reads configuration values from {@link System#getProperty(String)}.
 */
public final class SystemPropertiesConfigurationSource implements ConfigurationSource {
    /**
     * Creates an instance of {@link SystemPropertiesConfigurationSource}.
     */
    public SystemPropertiesConfigurationSource() {
    }

    @Override
    public String getProperty(String name) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        return System.getProperty(name);
    }

    @Override
    public boolean isMutable() {
        return true;
    }
}
