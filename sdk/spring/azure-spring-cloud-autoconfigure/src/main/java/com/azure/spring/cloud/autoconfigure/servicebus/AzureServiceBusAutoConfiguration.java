// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for a {@link ServiceBusClientBuilder}.
 */
@ConditionalOnClass(ServiceBusClientBuilder.class)
@ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, name = "enabled", matchIfMissing = true)
@Import({
    AzureServiceBusClientBuilderConfiguration.class,
    AzureServiceBusProducerClientConfiguration.class,
    AzureServiceBusConsumerClientConfiguration.class,
    AzureServiceBusProcessorConfiguration.class
})
@ConditionalOnBean(AzureConfigurationProperties.class)
public class AzureServiceBusAutoConfiguration extends AzureServiceConfigurationBase {


    public AzureServiceBusAutoConfiguration(AzureConfigurationProperties azureProperties) {
        super(azureProperties);
    }

    @Bean
    @ConfigurationProperties(AzureServiceBusProperties.PREFIX)
    public AzureServiceBusProperties azureServiceBusProperties(AzureConfigurationProperties azureProperties) {
        return loadProperties(this.azureProperties, new AzureServiceBusProperties());
    }

}
