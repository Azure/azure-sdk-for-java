// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.resourcemanager.connectionstring.EventHubsArmConnectionStringProvider;
import com.azure.spring.eventhubs.provisioning.EventHubsProvisioner;
import com.azure.spring.eventhubs.provisioning.arm.DefaultEventHubsProvisioner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 *
 */
@ConditionalOnProperty(prefix = AzureEventHubsProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(EventHubClientBuilder.class)
@ConditionalOnBean(AzureResourceManager.class)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
public class AzureEventHubsResourceManagerAutoConfiguration extends AzureServiceResourceManagerConfigurationBase {

    private final AzureEventHubsProperties eventHubsProperties;

    public AzureEventHubsResourceManagerAutoConfiguration(AzureResourceManager azureResourceManager,
                                                          AzureEventHubsProperties eventHubsProperties) {
        super(azureResourceManager);
        this.eventHubsProperties = eventHubsProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureEventHubsProperties.PREFIX, value = "namespace")
    @ConditionalOnMissingProperty(prefix = AzureEventHubsProperties.PREFIX, value = "connection-string")
    @Order
    public EventHubsArmConnectionStringProvider eventHubsArmConnectionStringProvider() {

        return new EventHubsArmConnectionStringProvider(this.azureResourceManager,
                                                       this.eventHubsProperties.getResource(),
                                                       this.eventHubsProperties.getNamespace());
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubsProvisioner eventHubsProvisioner() {
        return new DefaultEventHubsProvisioner(this.azureResourceManager, this.eventHubsProperties.getResource());
    }

}
