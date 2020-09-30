// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.microsoft.azure.spring.cloud.context.core.impl.ServiceBusTopicSubscriptionManager;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.integration.servicebus.factory.DefaultServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;

/**
 * An auto-configuration for Service Bus topic
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(TopicClient.class)
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
public class AzureServiceBusTopicAutoConfiguration {
    private static final String SERVICE_BUS_TOPIC = "ServiceBusTopic";
    private static final String NAMESPACE = "Namespace";

    
    @Autowired(required = false)
    private ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager;
    
    @Autowired(required = false)
    private ServiceBusNamespaceManager serviceBusNamespaceManager;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_TOPIC);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicClientFactory topicClientFactory(AzureServiceBusProperties serviceBusProperties) {
        String connectionString = serviceBusProperties.getConnectionString();
        DefaultServiceBusTopicClientFactory clientFactory =
            new DefaultServiceBusTopicClientFactory(serviceBusProperties.getConnectionString());

        if (serviceBusTopicSubscriptionManager != null && serviceBusNamespaceManager != null) {
            clientFactory.setNamespace(serviceBusProperties.getNamespace());
            clientFactory.setServiceBusNamespaceManager(serviceBusNamespaceManager);
            clientFactory.setServiceBusTopicSubscriptionManager(serviceBusTopicSubscriptionManager);
        } else {
            TelemetryCollector.getInstance().addProperty(SERVICE_BUS_TOPIC, NAMESPACE,
                ServiceBusUtils.getNamespace(connectionString));
        }

        return clientFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicOperation topicOperation(ServiceBusTopicClientFactory factory) {
        return new ServiceBusTopicTemplate(factory);
    }
}
