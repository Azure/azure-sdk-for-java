// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

/**
 * Represents a way to retrieve a configuration value.
 */
interface ConfigurationGetter {
    /**
     * Attempts to retrieve the configuration value.
     * @param configurationName Name of the configuration.
     * @return The configuration's value if found, null otherwise.
     */
    String getConfiguration(String configurationName);
}
