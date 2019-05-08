// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.configuration;

/**
 * Retrieves configuration from the environment variables.
 */
public final class EnvironmentConfigurationGetter extends ConfigurationGetter {
    private static final String LOG_MESSAGE = "Found configuration %s in the environment variables.";

    @Override
    public String getConfiguration(String configurationName) {
        try {
            return System.getenv(configurationName);
        } catch (SecurityException | NullPointerException ex) {
            return null;
        }
    }

    @Override
    String logMessage(String configurationName) {
        return String.format(LOG_MESSAGE, configurationName);
    }
}
