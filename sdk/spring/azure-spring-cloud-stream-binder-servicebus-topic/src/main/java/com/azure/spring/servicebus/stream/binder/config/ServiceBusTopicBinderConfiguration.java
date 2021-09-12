// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.config;

import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusTopicOperationAutoConfiguration;
import com.azure.spring.cloud.resourcemanager.core.impl.ServiceBusNamespaceCrud;
import com.azure.spring.cloud.resourcemanager.core.impl.ServiceBusTopicSubscriptionManager;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.azure.spring.servicebus.stream.binder.ServiceBusTopicMessageChannelBinder;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusTopicExtendedBindingProperties;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.servicebus.stream.binder.provisioning.ServiceBusTopicChannelResourceManagerProvisioner;
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
    AzureResourceManagerAutoConfiguration.class,
    AzureServiceBusResourceManagerAutoConfiguration.class,
    AzureServiceBusTopicOperationAutoConfiguration.class,
    ServiceBusTopicBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties({ AzureServiceBusProperties.class, ServiceBusTopicExtendedBindingProperties.class })
public class ServiceBusTopicBinderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusChannelProvisioner serviceBusChannelProvisioner(
        AzureServiceBusProperties serviceBusProperties,
        @Autowired(required = false) ServiceBusNamespaceManager serviceBusNamespaceManager,
        @Autowired(required = false) com.azure.spring.cloud.resourcemanager.core.impl.ServiceBusTopicCrud serviceBusTopicManager,
        @Autowired(required = false) ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager) {

        if (serviceBusNamespaceManager != null
                && serviceBusTopicManager != null
                && serviceBusTopicSubscriptionManager != null) {
            return new ServiceBusTopicChannelResourceManagerProvisioner(serviceBusNamespaceManager,
                                                                        serviceBusTopicManager,
                                                                        serviceBusTopicSubscriptionManager,
                                                                        serviceBusProperties.getNamespace());
        }
        return new ServiceBusChannelProvisioner();
    }

    @Bean
    public ServiceBusTopicMessageChannelBinder serviceBusTopicBinder(
        ServiceBusChannelProvisioner topicChannelProvisioner,
        ServiceBusTopicOperation serviceBusTopicOperation,
        ServiceBusTopicExtendedBindingProperties bindingProperties) {

        ServiceBusTopicMessageChannelBinder binder = new ServiceBusTopicMessageChannelBinder(null,
                                                                                             topicChannelProvisioner,
                                                                                             serviceBusTopicOperation);
        binder.setBindingProperties(bindingProperties);
        return binder;
    }
}
