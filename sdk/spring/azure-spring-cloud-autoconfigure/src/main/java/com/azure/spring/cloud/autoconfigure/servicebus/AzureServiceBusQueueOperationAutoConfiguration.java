// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.servicebus.core.DefaultServiceBusQueueClientFactory;
import com.azure.spring.servicebus.core.ServiceBusQueueClientFactory;
import com.azure.spring.servicebus.provisioning.ServiceBusQueueProvisioner;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueTemplate;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * An auto-configuration for Service Bus Queue.
 *
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnClass(ServiceBusQueueClientFactory.class)
@ConditionalOnExpression("${spring.cloud.azure.servicebus.enabled:true}")
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
public class AzureServiceBusQueueOperationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusClientBuilder.class)
    public ServiceBusQueueClientFactory queueClientFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                           ObjectProvider<ServiceBusQueueProvisioner> serviceBusQueueProvisioners) {
        DefaultServiceBusQueueClientFactory clientFactory = new DefaultServiceBusQueueClientFactory(serviceBusClientBuilder);
        clientFactory.setQueueProvisioner(serviceBusQueueProvisioners.getIfAvailable());
        return clientFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusMessageConverter messageConverter() {
        return new ServiceBusMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusQueueClientFactory.class)
    public ServiceBusQueueOperation queueOperation(ServiceBusQueueClientFactory factory,
                                                   ServiceBusMessageConverter messageConverter) {
        return new ServiceBusQueueTemplate(factory, messageConverter);
    }

}
