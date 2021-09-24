// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubProperties;
import com.azure.spring.cloud.autoconfigure.eventhubs.resourcemanager.DefaultEventHubProvisioner;
import com.azure.spring.cloud.resourcemanager.connectionstring.EventHubArmConnectionStringProvider;
import com.azure.spring.integration.eventhub.factory.EventHubProvisioner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 *
 */
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
    @ConditionalOnProperty("spring.cloud.azure.eventhub.namespace")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhub.connection-string:}')")
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
