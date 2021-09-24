// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * An auto-configuration for Event Hub, which provides all kinds of Event Hub clients.
 *
 */
@ConditionalOnClass(EventHubClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnExpression(" !T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.connection-string:}')"
                             + " or !T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.namespace:}')")
@Import({
    AzureEventHubClientBuilderConfiguration.class,
    AzureEventHubConsumerClientConfiguration.class,
    AzureEventHubProducerClientConfiguration.class,
    AzureBlobCheckpointStoreConfiguration.class,
    AzureEventProcessorClientConfiguration.class
})
public class AzureEventHubAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureEventHubAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureEventHubProperties.PREFIX)
    public AzureEventHubProperties azureEventHubProperties() {
        return loadProperties(this.azureGlobalProperties, new AzureEventHubProperties());
    }

}
