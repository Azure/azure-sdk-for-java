// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.cloud.context.core.api.AzureResourceMetadata;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicSubscriptionManager;
import com.azure.spring.core.util.Tuple;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.DefaultServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.factory.ServiceBusProvisioner;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
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
 * An auto-configuration for Service Bus topic
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
@ConditionalOnClass(value = {ServiceBusProcessorClient.class, ServiceBusTopicClientFactory.class})
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
public class AzureServiceBusTopicAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusTopicAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ ServiceBusNamespaceManager.class, AzureResourceMetadata.class })
    public ServiceBusTopicManager serviceBusTopicManager(AzureResourceMetadata azureResourceMetadata) {
        return new ServiceBusTopicManager(azureResourceMetadata);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ ServiceBusTopicManager.class, AzureResourceMetadata.class })
    public ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager(AzureResourceMetadata azureResourceMetadata) {
        return new ServiceBusTopicSubscriptionManager(azureResourceMetadata);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicClientFactory topicClientFactory(
        @Autowired(required = false) ServiceBusNamespaceManager namespaceManager,
        @Autowired(required = false) ServiceBusTopicManager topicManager,
        @Autowired(required = false) ServiceBusTopicSubscriptionManager topicSubscriptionManager,
        ServiceBusConnectionStringProvider connectionStringProvider,
        AzureServiceBusProperties properties) {

        if (connectionStringProvider == null) {
            LOGGER.info("No service bus connection string provided.");
            return null;
        }

        String connectionString = connectionStringProvider.getConnectionString();

        Assert.notNull(connectionString, "Service Bus connection string must not be null");

        DefaultServiceBusTopicClientFactory clientFactory = new DefaultServiceBusTopicClientFactory(connectionString, properties.getTransportType());
        clientFactory.setRetryOptions(properties.getRetryOptions());
        clientFactory.setNamespace(properties.getNamespace());
        clientFactory.setServiceBusProvisioner(new ServiceBusTopicProvisioner(namespaceManager, topicManager, topicSubscriptionManager));

        return clientFactory;
    }

    static class ServiceBusTopicProvisioner implements ServiceBusProvisioner {

        private final ServiceBusNamespaceManager namespaceManager;
        private final ServiceBusTopicManager topicManager;
        private final ServiceBusTopicSubscriptionManager subscriptionManager;

        ServiceBusTopicProvisioner(ServiceBusNamespaceManager namespaceManager,
                                   ServiceBusTopicManager topicManager,
                                   ServiceBusTopicSubscriptionManager subscriptionManager) {
            this.namespaceManager = namespaceManager;
            this.topicManager = topicManager;
            this.subscriptionManager = subscriptionManager;
        }

        @Override
        public void provisionNamespace(String namespace) {
            this.namespaceManager.create(namespace);
        }

        @Override
        public void provisionQueue(String namespace, String queue) {
            throw new UnsupportedOperationException("Can't provision queue in a topic client");
        }

        @Override
        public void provisionTopic(String namespace, String topic) {
            final ServiceBusNamespace serviceBusNamespace = namespaceManager.get(namespace);
            this.topicManager.create(Tuple.of(serviceBusNamespace, topic));
        }

        @Override
        public void provisionSubscription(String namespace, String topic, String subscription) {
            final ServiceBusNamespace serviceBusNamespace = namespaceManager.get(namespace);
            final Topic serviceBusTopic = topicManager.get(Tuple.of(serviceBusNamespace, topic));
            this.subscriptionManager.create(Tuple.of(serviceBusTopic, subscription));
        }
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
