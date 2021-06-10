// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.config;

import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusQueueAutoConfiguration;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusQueueManager;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.stream.binder.ServiceBusQueueMessageChannelBinder;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusQueueExtendedBindingProperties;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusQueueChannelResourceManagerProvisioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@Import({
    AzureEnvironmentAutoConfiguration.class,
    AzureContextAutoConfiguration.class,
    AzureServiceBusAutoConfiguration.class,
    AzureServiceBusQueueAutoConfiguration.class })
@EnableConfigurationProperties({ AzureServiceBusProperties.class, ServiceBusQueueExtendedBindingProperties.class })
public class ServiceBusQueueBinderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusChannelProvisioner serviceBusChannelProvisioner(
        AzureServiceBusProperties serviceBusProperties,
        @Autowired(required = false) ServiceBusNamespaceManager serviceBusNamespaceManager,
        @Autowired(required = false) ServiceBusQueueManager serviceBusQueueManager) {

        if (serviceBusNamespaceManager != null && serviceBusQueueManager != null) {
            return new ServiceBusQueueChannelResourceManagerProvisioner(serviceBusNamespaceManager,
                serviceBusQueueManager, serviceBusProperties.getNamespace());
        }
        return new ServiceBusChannelProvisioner();
    }

    @Bean
    public ServiceBusQueueMessageChannelBinder serviceBusQueueBinder(
        ServiceBusChannelProvisioner queueChannelProvisioner,
        ServiceBusQueueOperation serviceBusQueueOperation,
        ServiceBusQueueExtendedBindingProperties bindingProperties) {

        ServiceBusQueueMessageChannelBinder binder = new ServiceBusQueueMessageChannelBinder(null,
            queueChannelProvisioner, serviceBusQueueOperation);
        binder.setBindingProperties(bindingProperties);
        return binder;
    }
}
