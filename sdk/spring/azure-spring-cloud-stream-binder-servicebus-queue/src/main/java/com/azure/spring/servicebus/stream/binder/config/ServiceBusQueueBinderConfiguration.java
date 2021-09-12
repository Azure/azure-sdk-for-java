// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.config;

import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusQueueOperationAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.resourcemanager.DefaultServiceBusQueueProvisioner;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.stream.binder.ServiceBusQueueMessageChannelBinder;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusQueueExtendedBindingProperties;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusQueueChannelResourceManagerProvisioner;
import org.springframework.beans.factory.ObjectProvider;
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
    AzureResourceManagerAutoConfiguration.class,
    AzureServiceBusResourceManagerAutoConfiguration.class,
    AzureServiceBusAutoConfiguration.class,
    AzureServiceBusQueueOperationAutoConfiguration.class,
    ServiceBusQueueBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(ServiceBusQueueExtendedBindingProperties.class)
public class ServiceBusQueueBinderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusChannelProvisioner serviceBusChannelProvisioner(AzureServiceBusProperties serviceBusProperties,
                                                                     ObjectProvider<DefaultServiceBusQueueProvisioner> queueProvisioners) {

        if (queueProvisioners.getIfAvailable() != null) {
            return new ServiceBusQueueChannelResourceManagerProvisioner(serviceBusProperties.getNamespace(),
                                                                        queueProvisioners.getIfAvailable());
        }
        return new ServiceBusChannelProvisioner();
    }

    @Bean
    public ServiceBusQueueMessageChannelBinder serviceBusQueueBinder(ServiceBusChannelProvisioner channelProvisioner,
                                                                     ServiceBusQueueOperation serviceBusQueueOperation,
                                                                     ServiceBusQueueExtendedBindingProperties bindingProperties) {

        ServiceBusQueueMessageChannelBinder binder = new ServiceBusQueueMessageChannelBinder(null,
                                                                                             channelProvisioner,
                                                                                             serviceBusQueueOperation);
        binder.setBindingProperties(bindingProperties);
        return binder;
    }
}
