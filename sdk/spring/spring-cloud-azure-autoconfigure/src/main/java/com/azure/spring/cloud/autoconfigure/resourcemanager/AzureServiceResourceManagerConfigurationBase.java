// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration base class for all Azure SDK resource managers.
 */
@Configuration(proxyBeanMethods = false)
public abstract class AzureServiceResourceManagerConfigurationBase {

    protected AzureResourceManager azureResourceManager;

    /**
     * Create {@link AzureServiceResourceManagerConfigurationBase} instance
     * @param azureResourceManager the azure resource manager
     */
    public AzureServiceResourceManagerConfigurationBase(AzureResourceManager azureResourceManager) {
        this.azureResourceManager = azureResourceManager;
    }

}
