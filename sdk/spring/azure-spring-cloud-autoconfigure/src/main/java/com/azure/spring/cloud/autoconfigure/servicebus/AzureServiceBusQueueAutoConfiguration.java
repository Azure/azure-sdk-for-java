// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusQueueManager;
import com.azure.spring.cloud.telemetry.TelemetryCollector;
import com.azure.spring.integration.servicebus.factory.DefaultServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.servicebus.QueueClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

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

        if (serviceBusNamespaceManager != null && serviceBusQueueManager != null) {
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
    @ConditionalOnProperty("spring.cloud.azure.resource-group")
    public ServiceBusNamespaceManager serviceBusNamespaceManager(Azure azure, AzureProperties azureProperties) {
        return new ServiceBusNamespaceManager(azure, azureProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("spring.cloud.azure.resource-group")
    public ServiceBusQueueManager serviceBusQueueManager(AzureProperties azureProperties) {
        return new ServiceBusQueueManager(azureProperties);
    }
}
