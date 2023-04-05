// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config;


import com.azure.data.appconfiguration.ConfigurationClientBuilder;

/**
 * Creates Custom CustomClientBuilder for connecting to Azure App Configuration.
 */
public interface ConfigurationClientCustomizer {

    /**
     * Updates the ConfigurationClientBuilder for connecting to the given App Configuration.
     * @param builder ConfigurationClientBuilder
     * @param endpoint String
     */
    void customize(ConfigurationClientBuilder builder, String endpoint);

}
