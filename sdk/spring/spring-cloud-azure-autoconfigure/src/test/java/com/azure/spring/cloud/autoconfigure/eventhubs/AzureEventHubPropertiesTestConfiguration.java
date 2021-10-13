// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties
class AzureEventHubPropertiesTestConfiguration {

    @Bean
    @ConfigurationProperties(AzureEventHubProperties.PREFIX)
    public AzureEventHubProperties azureEventHubProperties() {
        return new AzureEventHubProperties();
    }
}
