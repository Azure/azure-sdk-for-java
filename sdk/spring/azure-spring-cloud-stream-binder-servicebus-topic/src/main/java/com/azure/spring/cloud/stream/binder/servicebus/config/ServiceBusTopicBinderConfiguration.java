// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.config;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusProperties;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusTopicExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusTopicChannelResourceManagerProvisioner;
import com.azure.spring.servicebus.core.ServiceBusTopicProvisioner;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicOperation;
import com.azure.spring.cloud.stream.binder.servicebus.ServiceBusTopicMessageChannelBinder;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusTopicOperationAutoConfiguration;
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
    AzureServiceBusAutoConfiguration.class,
    AzureResourceManagerAutoConfiguration.class,
    AzureServiceBusResourceManagerAutoConfiguration.class,
    AzureServiceBusTopicOperationAutoConfiguration.class,
    ServiceBusTopicBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(ServiceBusTopicExtendedBindingProperties.class)
public class ServiceBusTopicBinderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusTopicProvisioner.class)
    public ServiceBusChannelProvisioner serviceBusChannelArmProvisioner(AzureServiceBusProperties serviceBusProperties,
                                                                        ServiceBusTopicProvisioner topicProvisioner) {

        return new ServiceBusTopicChannelResourceManagerProvisioner(serviceBusProperties.getNamespace(),
                                                                    topicProvisioner);

    }

    @Bean
    @ConditionalOnMissingBean({ ServiceBusChannelProvisioner.class, ServiceBusTopicProvisioner.class })
    public ServiceBusChannelProvisioner serviceBusChannelProvisioner() {
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
