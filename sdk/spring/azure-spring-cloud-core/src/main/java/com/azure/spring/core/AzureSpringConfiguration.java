// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;

import com.azure.core.util.Configuration;
import com.azure.spring.core.properties.AzureProperties;

/**
 * Extends the {@link Configuration} to provide Azure Spring related configurations.
 */
public class AzureSpringConfiguration extends Configuration {

    private final AzureProperties azureProperties;

    public AzureSpringConfiguration(AzureProperties azureProperties) {
        this.azureProperties = azureProperties;
    }



}