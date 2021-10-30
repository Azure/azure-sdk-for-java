// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.servicebus.core.processor.DefaultServiceBusNamespaceTopicProcessorClientFactory;
import com.azure.spring.servicebus.core.processor.ServiceBusNamespaceTopicProcessorClientFactory;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicOperation;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicTemplate;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * An auto-configuration for Service Bus topic
 */
@Configuration
@ConditionalOnClass(ServiceBusNamespaceTopicProcessorClientFactory.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
public class AzureServiceBusTopicOperationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean()
    @ConditionalOnBean(ServiceBusClientBuilder.class)
    public ServiceBusNamespaceTopicProcessorClientFactory topicClientFactory(ServiceBusClientBuilder serviceBusClientBuilder) {
//                                                           ObjectProvider<ServiceBusTopicProvisioner> serviceBusTopicProvisioners) {
        DefaultServiceBusNamespaceTopicProcessorClientFactory clientFactory = new DefaultServiceBusNamespaceTopicProcessorClientFactory(serviceBusClientBuilder);

        // TODO (xiada) the application id should be different for spring integration

//        clientFactory.setTopicProvisioner(serviceBusTopicProvisioners.getIfAvailable());
        return clientFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusMessageConverter messageConverter() {
        return new ServiceBusMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusNamespaceTopicProcessorClientFactory.class)
    public ServiceBusTopicOperation topicOperation(ServiceBusNamespaceTopicProcessorClientFactory factory,
                                                   ServiceBusMessageConverter messageConverter) {
        return new ServiceBusTopicTemplate(factory, messageConverter);
    }
}
