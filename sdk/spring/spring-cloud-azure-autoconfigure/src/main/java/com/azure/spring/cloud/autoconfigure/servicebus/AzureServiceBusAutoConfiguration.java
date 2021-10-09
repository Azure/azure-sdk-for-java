// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for a {@link ServiceBusClientBuilder}.
 */
@ConditionalOnClass(ServiceBusClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@Import({
    AzureServiceBusClientBuilderConfiguration.class,
    AzureServiceBusProducerClientConfiguration.class,
    AzureServiceBusConsumerClientConfiguration.class,
    AzureServiceBusProcessorConfiguration.class
})
public class AzureServiceBusAutoConfiguration extends AzureServiceConfigurationBase {


    public AzureServiceBusAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureServiceBusProperties.PREFIX)
    public AzureServiceBusProperties azureServiceBusProperties(AzureGlobalProperties azureProperties) {
        return loadProperties(this.azureGlobalProperties, new AzureServiceBusProperties());
    }

}
