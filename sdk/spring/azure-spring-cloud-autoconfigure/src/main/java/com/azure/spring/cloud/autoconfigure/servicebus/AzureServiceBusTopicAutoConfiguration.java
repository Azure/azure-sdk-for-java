// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicSubscriptionManager;
import com.azure.spring.cloud.telemetry.TelemetryCollector;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.DefaultServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.factory.ServiceBusConnectionStringProvider;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import com.microsoft.azure.servicebus.TopicClient;
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

import javax.annotation.PostConstruct;

import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusUtils.getNamespace;

/**
 * An auto-configuration for Service Bus topic
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
@ConditionalOnClass(TopicClient.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
public class AzureServiceBusTopicAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusTopicAutoConfiguration.class);

    private static final String SERVICE_BUS_TOPIC = "ServiceBusTopic";
    private static final String NAMESPACE = "Namespace";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_TOPIC);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusNamespaceManager.class)
    public ServiceBusTopicManager serviceBusTopicManager(AzureProperties azureProperties) {
        return new ServiceBusTopicManager(azureProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusTopicManager.class)
    public ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager(AzureProperties azureProperties) {
        return new ServiceBusTopicSubscriptionManager(azureProperties);
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

        DefaultServiceBusTopicClientFactory clientFactory = new DefaultServiceBusTopicClientFactory(connectionString);
        clientFactory.setNamespace(properties.getNamespace());
        clientFactory.setServiceBusNamespaceManager(namespaceManager);
        clientFactory.setServiceBusTopicManager(topicManager);
        clientFactory.setServiceBusTopicSubscriptionManager(topicSubscriptionManager);

        TelemetryCollector.getInstance().addProperty(SERVICE_BUS_TOPIC, NAMESPACE, getNamespace(connectionString));

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
