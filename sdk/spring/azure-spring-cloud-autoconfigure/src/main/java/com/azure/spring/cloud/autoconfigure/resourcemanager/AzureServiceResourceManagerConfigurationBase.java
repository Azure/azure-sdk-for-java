// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration base class for all Azure SDK resource managers.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
public abstract class AzureServiceResourceManagerConfigurationBase {

    protected AzureResourceManager azureResourceManager;

    public AzureServiceResourceManagerConfigurationBase(AzureResourceManager azureResourceManager) {
        this.azureResourceManager = azureResourceManager;
    }

}
