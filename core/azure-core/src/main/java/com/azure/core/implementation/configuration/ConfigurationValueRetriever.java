// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

/**
 * Represents a way to retrieve a configuration value.
 */
interface ConfigurationValueRetriever {
    /**
     * Attempts to retrieve the configuration value.
     * @param name Name of the configuration.
     * @return The configuration's value if found, null otherwise.
     */
    String retrieve(String name);
}
