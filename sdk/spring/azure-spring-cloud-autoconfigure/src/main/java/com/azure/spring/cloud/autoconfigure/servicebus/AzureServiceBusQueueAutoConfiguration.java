// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.spring.cloud.context.core.api.AzureResourceMetadata;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusQueueManager;
import com.azure.spring.core.util.Tuple;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import com.azure.spring.servicebus.core.DefaultServiceBusQueueClientFactory;
import com.azure.spring.servicebus.support.ServiceBusProvisioner;
import com.azure.spring.servicebus.core.ServiceBusQueueClientFactory;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * An auto-configuration for Service Bus Queue.
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
@ConditionalOnClass(value = { ServiceBusProcessorClient.class, ServiceBusQueueClientFactory.class })
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
public class AzureServiceBusQueueAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusQueueAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ ServiceBusNamespaceManager.class, AzureResourceMetadata.class })
    public ServiceBusQueueManager serviceBusQueueManager(AzureResourceMetadata azureResourceMetadata) {
        return new ServiceBusQueueManager(azureResourceMetadata);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusQueueClientFactory queueClientFactory(
        ServiceBusConnectionStringProvider connectionStringProvider,
        @Autowired(required = false) ServiceBusNamespaceManager namespaceManager,
        @Autowired(required = false) ServiceBusQueueManager queueManager,
        AzureServiceBusProperties properties) {

        if (connectionStringProvider == null) {
            LOGGER.info("No service bus connection string provided.");
            return null;
        }

        String connectionString = connectionStringProvider.getConnectionString();

        Assert.notNull(connectionString, "Service Bus connection string must not be null");

        DefaultServiceBusQueueClientFactory clientFactory = new DefaultServiceBusQueueClientFactory(connectionString, properties.getTransportType());
        clientFactory.setRetryOptions(properties.getRetryOptions());
        clientFactory.setServiceBusProvisioner(new ServiceBusQueueProvisioner(namespaceManager, queueManager) {
        });

        return clientFactory;
    }

    static class ServiceBusQueueProvisioner implements ServiceBusProvisioner {

        private final ServiceBusNamespaceManager namespaceManager;
        private final ServiceBusQueueManager queueManager;

        ServiceBusQueueProvisioner(ServiceBusNamespaceManager namespaceManager,
                                   ServiceBusQueueManager queueManager) {
            this.namespaceManager = namespaceManager;
            this.queueManager = queueManager;
        }

        @Override
        public void provisionNamespace(String namespace) {
            this.namespaceManager.create(namespace);
        }

        @Override
        public void provisionQueue(String namespace, String queue) {
            final ServiceBusNamespace serviceBusNamespace = namespaceManager.get(namespace);
            this.queueManager.create(Tuple.of(serviceBusNamespace, queue));
        }

        @Override
        public void provisionTopic(String namespace, String topic) {
            throw new UnsupportedOperationException("Can't provision topic in a queue client");
        }

        @Override
        public void provisionSubscription(String namespace, String topic, String subscription) {
            throw new UnsupportedOperationException("Can't provision subscription in a queue client");
        }
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
