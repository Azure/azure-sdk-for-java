// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.configuration;

/**
 * Represents a way to retrieve a configuration value.
 */
abstract class ConfigurationGetter {
    /**
     * Attempts to retrieve the configuration value.
     * @param configurationName Name of the configuration.
     * @return The configuration's value if found, null otherwise.
     */
    public abstract String getConfiguration(String configurationName);

    abstract String logMessage(String configurationName);
}
