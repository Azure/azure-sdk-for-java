// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Service Bus support.
 *
 * @since 4.0.0
 */
@ConditionalOnClass(ServiceBusClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@Import({
    AzureServiceBusClientBuilderConfiguration.class,
    AzureServiceBusProducerClientConfiguration.class,
    AzureServiceBusConsumerClientConfiguration.class,
    AzureServiceBusProcessorClientConfiguration.class
})
public class AzureServiceBusAutoConfiguration extends AzureServiceConfigurationBase {


    AzureServiceBusAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureServiceBusProperties.PREFIX)
    AzureServiceBusProperties azureServiceBusProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureServiceBusProperties());
    }

}
