// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

/**
 * Manages the global configuration store.
 */
public final class ConfigurationManager {
    private static Configuration configuration;

    static {
        configuration = new Configuration();
        for (String config : BaseConfigurations.DEFAULT_CONFIGURATIONS) {
            configuration.load(config);
        }
    }

    /**
     * @return the global configuration store.
     */
    public static Configuration getConfiguration() {
        return configuration;
    }
}
