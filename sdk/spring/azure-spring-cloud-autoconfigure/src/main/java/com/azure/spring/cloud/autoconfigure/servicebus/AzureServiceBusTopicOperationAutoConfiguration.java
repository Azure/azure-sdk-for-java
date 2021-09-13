// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.DefaultServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicProvisioner;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * An auto-configuration for Service Bus topic
 *
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnClass(ServiceBusTopicClientFactory.class)
@ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, value = "enabled", matchIfMissing = true)
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
public class AzureServiceBusTopicOperationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnBean(ServiceBusClientBuilder.class)
    public ServiceBusTopicClientFactory topicClientFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                           ObjectProvider<ServiceBusTopicProvisioner> serviceBusTopicProvisioners) {
        DefaultServiceBusTopicClientFactory clientFactory = new DefaultServiceBusTopicClientFactory(serviceBusClientBuilder);
        // TODO (xiada) the application id should be different for spring integration

        clientFactory.setTopicProvisioner(serviceBusTopicProvisioners.getIfAvailable());
        return clientFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusMessageConverter messageConverter() {
        return new ServiceBusMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusTopicClientFactory.class)
    public ServiceBusTopicOperation topicOperation(ServiceBusTopicClientFactory factory,
                                                   ServiceBusMessageConverter messageConverter) {
        return new ServiceBusTopicTemplate(factory, messageConverter);
    }
}
