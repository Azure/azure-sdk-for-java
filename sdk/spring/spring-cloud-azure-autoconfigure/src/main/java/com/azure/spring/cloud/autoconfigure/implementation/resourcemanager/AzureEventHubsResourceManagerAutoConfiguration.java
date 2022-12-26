// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.resourcemanager.implementation.connectionstring.EventHubsArmConnectionStringProvider;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.DefaultEventHubsProvisioner;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.EventHubsProvisioner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Event Hubs resource manager support.
 *
 * @since 4.0.0
 */
@ConditionalOnProperty(prefix = AzureEventHubsProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(AzureResourceManager.class)
@ConditionalOnClass(EventHubsProvisioner.class)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
@EnableConfigurationProperties(EventHubsResourceMetadata.class)
public class AzureEventHubsResourceManagerAutoConfiguration extends AzureServiceResourceManagerConfigurationBase {

    private final EventHubsResourceMetadata resourceMetadata;

    AzureEventHubsResourceManagerAutoConfiguration(AzureResourceManager azureResourceManager,
                                                   EventHubsResourceMetadata resourceMetadata) {
        super(azureResourceManager);
        this.resourceMetadata = resourceMetadata;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureEventHubsProperties.PREFIX, value = "namespace")
    @ConditionalOnMissingProperty(prefix = AzureEventHubsProperties.PREFIX, value = "connection-string")
    @Order
    EventHubsArmConnectionStringProvider eventHubsArmConnectionStringProvider() {

        return new EventHubsArmConnectionStringProvider(this.azureResourceManager, resourceMetadata,
            resourceMetadata.getName());
    }

    @Bean
    @ConditionalOnMissingBean
    EventHubsProvisioner eventHubsProvisioner() {
        return new DefaultEventHubsProvisioner(this.azureResourceManager, this.resourceMetadata);
    }

}
