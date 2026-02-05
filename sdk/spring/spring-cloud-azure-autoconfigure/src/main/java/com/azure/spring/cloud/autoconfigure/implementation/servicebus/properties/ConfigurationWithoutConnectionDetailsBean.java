// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnMissingBean(type = "com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusConnectionDetails")
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.servicebus", name = {"connection-string", "namespace"})
class ConfigurationWithoutConnectionDetailsBean {

    private final AzureGlobalProperties azureGlobalProperties;

    ConfigurationWithoutConnectionDetailsBean(AzureGlobalProperties azureGlobalProperties) {
        this.azureGlobalProperties = azureGlobalProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(AzureServiceBusProperties.PREFIX)
    AzureServiceBusProperties azureServiceBusProperties() {
        return AzureGlobalPropertiesUtils.loadProperties(azureGlobalProperties, new AzureServiceBusProperties());
    }

}
