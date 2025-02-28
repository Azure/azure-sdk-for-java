// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnMissingBean(type = "com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusConnectionDetails")
class ConfigurationWithoutConnectionDetailsBean {

    private final AzureGlobalProperties azureGlobalProperties;

    public ConfigurationWithoutConnectionDetailsBean(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
    }

    @Bean
    @ConfigurationProperties(AzureServiceBusProperties.PREFIX)
    AzureServiceBusProperties azureServiceBusProperties() {
        return AzureGlobalPropertiesUtils.loadProperties(azureGlobalProperties, new AzureServiceBusProperties());
    }

}
