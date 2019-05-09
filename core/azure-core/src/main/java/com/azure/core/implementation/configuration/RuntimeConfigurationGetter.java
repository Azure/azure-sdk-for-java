// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

/**
 * Retrieves configurations from the application runtime.
 */
public final class RuntimeConfigurationGetter extends ConfigurationGetter {
    private static final String LOG_MESSAGE = "Found configuration %s in the runtime variables.";

    @Override
    public String getConfiguration(String configurationName) {
        // Should these exceptions get caught?
        try {
            return System.getProperty(configurationName);
        } catch (SecurityException | NullPointerException | IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    boolean isLogWorthy() {
        return true;
    }

    @Override
    String logMessage(String configurationName) {
        return String.format(LOG_MESSAGE, configurationName);
    }
}
