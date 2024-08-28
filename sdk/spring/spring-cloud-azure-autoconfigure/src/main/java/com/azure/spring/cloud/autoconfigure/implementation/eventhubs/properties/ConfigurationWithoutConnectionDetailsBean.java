// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConditionalOnMissingBean(type = "com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsConnectionDetails")
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "connection-string", "namespace" })
class ConfigurationWithoutConnectionDetailsBean {

    private final AzureGlobalProperties globalProperties;

    ConfigurationWithoutConnectionDetailsBean(AzureGlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    @Bean
    @ConfigurationProperties(AzureEventHubsProperties.PREFIX)
    AzureEventHubsProperties azureEventHubsProperties() {
        return AzureGlobalPropertiesUtils.loadProperties(this.globalProperties, new AzureEventHubsProperties());
    }

}
