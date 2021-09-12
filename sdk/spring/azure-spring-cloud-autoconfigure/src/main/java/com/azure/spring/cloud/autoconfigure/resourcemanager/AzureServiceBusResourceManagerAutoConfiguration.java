// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.servicebus.resourcemanager.ServiceBusQueueProvisioner;
import com.azure.spring.cloud.autoconfigure.servicebus.resourcemanager.ServiceBusTopicProvisioner;
import com.azure.spring.cloud.resourcemanager.connectionstring.ServiceBusArmConnectionStringProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * An auto-configuration for Service Bus
 *
 * @author Warren Zhu
 */
@ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, value = "enabled", matchIfMissing = true)
public class AzureServiceBusResourceManagerAutoConfiguration extends AzureServiceResourceManagerConfigurationBase {

    private final AzureServiceBusProperties serviceBusProperties;

    public AzureServiceBusResourceManagerAutoConfiguration(AzureResourceManager azureResourceManager,
                                                           AzureServiceBusProperties serviceBusProperties) {
        super(azureResourceManager);
        this.serviceBusProperties = serviceBusProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusQueueProvisioner serviceBusQueueProvisioner() {
        return new ServiceBusQueueProvisioner(this.azureResourceManager, this.serviceBusProperties.getResource());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicProvisioner serviceBusTopicProvisioner() {
        return new ServiceBusTopicProvisioner(this.azureResourceManager, this.serviceBusProperties.getResource());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, value = "namespace")
    @Order
    public ServiceBusArmConnectionStringProvider serviceBusArmConnectionStringProvider() {
        return new ServiceBusArmConnectionStringProvider(this.azureResourceManager,
                                                         this.serviceBusProperties.getResource(),
                                                         this.serviceBusProperties.getNamespace());
    }

}

