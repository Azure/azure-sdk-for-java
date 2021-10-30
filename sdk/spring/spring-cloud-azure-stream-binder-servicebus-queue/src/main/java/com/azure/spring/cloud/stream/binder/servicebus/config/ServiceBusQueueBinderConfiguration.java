// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.config;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusQueueMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.stream.binder.servicebus.ServiceBusQueueMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusQueueExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusQueueChannelResourceManagerProvisioner;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.provisioning.ServiceBusQueueProvisioner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
    AzureGlobalPropertiesAutoConfiguration.class,
    AzureResourceManagerAutoConfiguration.class,
    AzureServiceBusResourceManagerAutoConfiguration.class,
    AzureServiceBusAutoConfiguration.class,
    AzureServiceBusQueueMessagingAutoConfiguration.class,
    ServiceBusQueueBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(ServiceBusQueueExtendedBindingProperties.class)
public class ServiceBusQueueBinderConfiguration {

    @Bean
    @ConditionalOnBean({ ServiceBusQueueProvisioner.class, AzureServiceBusProperties.class })
    public ServiceBusChannelProvisioner serviceBusChannelArmProvisioner(AzureServiceBusProperties serviceBusProperties,
                                                                        ServiceBusQueueProvisioner queueProvisioner) {


        return new ServiceBusQueueChannelResourceManagerProvisioner(serviceBusProperties.getNamespace(),
                                                                    queueProvisioner);
    }

    @Bean
    @ConditionalOnMissingBean(ServiceBusQueueProvisioner.class)
    public ServiceBusChannelProvisioner serviceBusChannelProvisioner() {
        return new ServiceBusChannelProvisioner();
    }


    @Bean
    public ServiceBusQueueMessageChannelBinder serviceBusQueueBinder(ServiceBusChannelProvisioner channelProvisioner,
                                                                     ServiceBusQueueExtendedBindingProperties bindingProperties,
                                                                     ObjectProvider<NamespaceProperties> namespaceProperties) {

        ServiceBusQueueMessageChannelBinder binder = new ServiceBusQueueMessageChannelBinder(null,
                                                                                             channelProvisioner);
        binder.setBindingProperties(bindingProperties);
        binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
        return binder;
    }
}
