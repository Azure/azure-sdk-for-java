// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
abstract class AzureServiceResourceManagerConfigurationBase {

    /**
     * The resource manager.
     */
    protected AzureResourceManager azureResourceManager;

    AzureServiceResourceManagerConfigurationBase(AzureResourceManager azureResourceManager) {
        this.azureResourceManager = azureResourceManager;
    }

}
