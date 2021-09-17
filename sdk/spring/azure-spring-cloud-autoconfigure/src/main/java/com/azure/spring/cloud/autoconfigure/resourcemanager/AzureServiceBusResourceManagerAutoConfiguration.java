// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.servicebus.resourcemanager.DefaultServiceBusQueueProvisioner;
import com.azure.spring.cloud.autoconfigure.servicebus.resourcemanager.DefaultServiceBusTopicProvisioner;
import com.azure.spring.cloud.resourcemanager.connectionstring.ServiceBusArmConnectionStringProvider;
import com.azure.spring.servicebus.core.ServiceBusQueueProvisioner;
import com.azure.spring.servicebus.core.ServiceBusTopicProvisioner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * An auto-configuration for Service Bus
 *
 */
@ConditionalOnExpression("${spring.cloud.azure.servicebus.enabled:true}")
@ConditionalOnBean(AzureResourceManager.class)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
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
        return new DefaultServiceBusQueueProvisioner(this.azureResourceManager, this.serviceBusProperties.getResource());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicProvisioner serviceBusTopicProvisioner() {
        return new DefaultServiceBusTopicProvisioner(this.azureResourceManager, this.serviceBusProperties.getResource());
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

