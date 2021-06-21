// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;


import com.azure.data.appconfiguration.ConfigurationClientBuilder;

/**
 * Creates Custom CustomClientBuilder for connecting to Azure App Configuration.
 */
public interface ConfigurationClientBuilderSetup {

    void setup(ConfigurationClientBuilder builder, String endpoint);

}
