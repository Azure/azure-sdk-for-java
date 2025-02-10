// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import com.azure.spring.cloud.core.properties.AzureProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration base class for all Azure SDK configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
public abstract class AzureServiceConfigurationBase {

    private final AzureGlobalProperties azureGlobalProperties;

    protected AzureServiceConfigurationBase(AzureGlobalProperties azureProperties) {
        this.azureGlobalProperties = azureProperties;
    }

    /**
     * Load the default value to an Azure Service properties from the global Azure properties.
     *
     * @param source The global Azure properties.
     * @param target The properties of an Azure Service, such as Event Hubs properties. Some common components of the
     *               service's properties have default value as set to the global properties. For example, the proxy of
     *               the Event Hubs properties takes the proxy set to the global Azure properties as default.
     * @param <T> The type of the properties of an Azure Service.
     * @return The Azure Service's properties.
     */
    protected <T extends AzureProperties> T loadProperties(AzureGlobalProperties source, T target) {
        return AzureGlobalPropertiesUtils.loadProperties(source, target);
    }

    /**
     * Get the {@link AzureGlobalProperties}.
     *
     * @return the global properties.
     */
    protected AzureGlobalProperties getAzureGlobalProperties() {
        return azureGlobalProperties;
    }
}
