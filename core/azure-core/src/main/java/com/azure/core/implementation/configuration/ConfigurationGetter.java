// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

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

    /**
     * Indicates if retrieving a configuration with this getter is log worthy.
     * @return True if a message should be logged when a configuration is retrieved from this getter.
     */
    abstract boolean isLogWorthy();

    /**
     * Creates the message that is logged.
     * @param configurationName Name of the found configuration.
     * @return The message that is logged.
     */
    abstract String logMessage(String configurationName);
}
