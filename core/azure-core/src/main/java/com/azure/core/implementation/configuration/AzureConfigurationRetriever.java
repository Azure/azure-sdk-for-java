// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

import com.azure.core.implementation.util.ImplUtils;

/**
 * Retrieves configuration values.
 */
public final class AzureConfigurationRetriever {
    private static ConfigurationValueRetriever[] retrievers;

    static {
        // Need a global off-switch for the configuration manager.
        // Clients handle opt-out by having defaults set in the client and allowing those defaults to be overridden.
        retrievers = new ConfigurationValueRetriever[] {
            new RuntimeConfigurationValueRetriever(),
            new EnvironmentConfigurationValueRetriever()};
    }

    /**
     * Sets where the configurations are search for and in which order. The {@code AzureConfigurationRetriever} uses the
     * {@link ConfigurationValueRetriever configuration value retrievers} in sequential order.
     * @param configurationGetters List of {@code ConfigurationGetter ConfigurationGetters} that the manager uses to
     *                             search for configuration values.
     */
    public static void retrievers(ConfigurationValueRetriever... configurationGetters) {
        AzureConfigurationRetriever.retrievers = configurationGetters;
    }

    /**
     * Retrieves the configuration using the {@link ConfigurationValueRetriever configuration value retrievers}.
     *
     * The default search order used is the following.
     * <ol>
     *     <li>{@link RuntimeConfigurationValueRetriever}</li>
     *     <li>{@link EnvironmentConfigurationValueRetriever}</li>
     * </ol>
     *
     * Switch the search using {@link AzureConfigurationRetriever#retrievers(ConfigurationValueRetriever...) retrievers}.
     * @param name Name of the configuration being retrieved.
     * @return The configuration value from the first place it was found, if not found null.
     */
    public static String retrieve(String name) {
        for (ConfigurationValueRetriever retriever : retrievers) {
            String configurationValue = retriever.retrieve(name);
            if (!ImplUtils.isNullOrEmpty(configurationValue)) {
                return configurationValue;
            }
        }

        return null;
    }
}
