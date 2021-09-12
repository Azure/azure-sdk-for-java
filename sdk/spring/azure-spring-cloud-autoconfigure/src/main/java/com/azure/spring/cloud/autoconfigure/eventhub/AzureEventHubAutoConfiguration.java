// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 *
 * @author Warren Zhu
 */

@ConditionalOnClass(EventHubClientBuilder.class)
@ConditionalOnProperty(prefix = AzureEventHubProperties.PREFIX, name = "enabled", matchIfMissing = true)
@Import({
    AzureEventHubClientConfiguration.class,
    AzureBlobCheckpointStoreConfiguration.class,
    AzureEventProcessorClientConfiguration.class
})
public class AzureEventHubAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureEventHubAutoConfiguration(AzureConfigurationProperties azureProperties) {
        super(azureProperties);
    }

    @Bean
    @ConfigurationProperties(AzureEventHubProperties.PREFIX)
    public AzureEventHubProperties azureEventHubProperties() {
        return copyProperties(this.azureProperties, new AzureEventHubProperties());
    }

}
