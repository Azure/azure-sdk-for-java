// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

// TODO (alzimmer): How is it being determined which global configuration store is connected to by default?

/**
 * Retrieves configurations from the global configuration store.
 */
public final class ConfigurationStoreConfigurationGetter extends ConfigurationGetter {
    private static final String LOG_MESSAGE = "Found configuration %s in the global configuration store.";

    @Override
    public String getConfiguration(String configurationName) {
        return "";
    }

    @Override
    boolean isLogWorthy() {
        return false;
    }

    @Override
    String logMessage(String configurationName) {
        return String.format(LOG_MESSAGE, configurationName);
    }
}
