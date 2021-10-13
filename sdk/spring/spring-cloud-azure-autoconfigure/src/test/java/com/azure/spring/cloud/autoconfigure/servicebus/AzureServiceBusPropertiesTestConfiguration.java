// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties
class AzureServiceBusPropertiesTestConfiguration {

    @Bean
    @ConfigurationProperties(AzureServiceBusProperties.PREFIX)
    public AzureServiceBusProperties azureEventHubProperties() {
        return new AzureServiceBusProperties();
    }
}
