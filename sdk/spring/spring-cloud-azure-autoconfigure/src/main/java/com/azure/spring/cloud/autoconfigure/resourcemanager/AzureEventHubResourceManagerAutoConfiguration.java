// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.cloud.resourcemanager.connectionstring.EventHubArmConnectionStringProvider;
import com.azure.spring.eventhubs.provisioning.EventHubProvisioner;
import com.azure.spring.eventhubs.provisioning.arm.DefaultEventHubProvisioner;
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
@ConditionalOnProperty(prefix = AzureEventHubProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(EventHubClientBuilder.class)
@ConditionalOnBean(AzureResourceManager.class)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
public class AzureEventHubResourceManagerAutoConfiguration extends AzureServiceResourceManagerConfigurationBase {

    private final AzureEventHubProperties eventHubProperties;

    public AzureEventHubResourceManagerAutoConfiguration(AzureResourceManager azureResourceManager,
                                                         AzureEventHubProperties eventHubProperties) {
        super(azureResourceManager);
        this.eventHubProperties = eventHubProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureEventHubProperties.PREFIX, value = "namespace")
    @ConditionalOnMissingProperty(prefix = AzureEventHubProperties.PREFIX, value = "connection-string")
    @Order
    public EventHubArmConnectionStringProvider eventHubArmConnectionStringProvider() {

        return new EventHubArmConnectionStringProvider(this.azureResourceManager,
                                                       this.eventHubProperties.getResource(),
                                                       this.eventHubProperties.getNamespace());
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubProvisioner eventHubProvisioner() {
        return new DefaultEventHubProvisioner(this.azureResourceManager, this.eventHubProperties.getResource());
    }

}
