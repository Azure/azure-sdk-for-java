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

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.microsoft.azure.spring.cloud.context.core.impl.ServiceBusQueueManager;
import com.microsoft.azure.spring.cloud.context.core.impl.ServiceBusTopicManager;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.integration.servicebus.factory.DefaultServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;

/**
 * An auto-configuration for Service Bus queue
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(value = { QueueClient.class, ServiceBusQueueClientFactory.class })
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
public class AzureServiceBusQueueAutoConfiguration {
    private static final String SERVICE_BUS_QUEUE = "ServiceBusQueue";
    private static final String NAMESPACE = "Namespace";

    @Autowired(required = false)
    private ServiceBusNamespaceManager serviceBusNamespaceManager;

    @Autowired(required = false)
    private ServiceBusQueueManager serviceBusQueueManager;

    @Autowired(required = false)
    private ServiceBusTopicManager serviceBusTopicManager;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_QUEUE);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusQueueClientFactory queueClientFactory(AzureServiceBusProperties serviceBusProperties) {
        String connectionString = serviceBusProperties.getConnectionString();
        DefaultServiceBusQueueClientFactory clientFactory = new DefaultServiceBusQueueClientFactory(
                serviceBusProperties.getConnectionString());

        if (serviceBusNamespaceManager != null && serviceBusQueueManager != null && serviceBusTopicManager != null) {
            clientFactory.setServiceBusNamespaceManager(serviceBusNamespaceManager);
            clientFactory.setServiceBusQueueManager(serviceBusQueueManager);
            clientFactory.setNamespace(serviceBusProperties.getNamespace());
        } else {
            TelemetryCollector.getInstance().addProperty(SERVICE_BUS_QUEUE, NAMESPACE,
                    ServiceBusUtils.getNamespace(connectionString));
        }

        return clientFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusQueueOperation queueOperation(ServiceBusQueueClientFactory factory) {
        return new ServiceBusQueueTemplate(factory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusNamespaceManager serviceBusNamespaceManager(Azure azure, AzureProperties azureProperties) {
        return new ServiceBusNamespaceManager(azure, azureProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusQueueManager serviceBusQueueManager(AzureProperties azureProperties) {
        return new ServiceBusQueueManager(azureProperties);
    }
}
