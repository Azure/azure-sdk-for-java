// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.AzurePropertiesUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration base class for all Azure SDK configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
public abstract class AzureServiceConfigurationBase {

    protected AzureGlobalProperties azureGlobalProperties;

    public AzureServiceConfigurationBase(AzureGlobalProperties azureProperties) {
        this.azureGlobalProperties = azureProperties;
    }

    protected <T extends AzureProperties> T loadProperties(AzureGlobalProperties source, T target) {
        AzurePropertiesUtils.copyAzureProperties(source, target);
        return target;
    }
}
