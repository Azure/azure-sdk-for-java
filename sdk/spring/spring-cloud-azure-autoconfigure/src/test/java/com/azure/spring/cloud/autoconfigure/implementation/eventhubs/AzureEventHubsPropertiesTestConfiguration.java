// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties
class AzureEventHubsPropertiesTestConfiguration {

    @Bean
    @ConfigurationProperties(AzureEventHubsProperties.PREFIX)
    AzureEventHubsProperties azureEventHubsProperties() {
        return new AzureEventHubsProperties();
    }
}
