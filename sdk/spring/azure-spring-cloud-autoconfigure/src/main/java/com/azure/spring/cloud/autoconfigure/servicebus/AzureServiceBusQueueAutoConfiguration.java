// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusQueueManager;
import com.azure.spring.cloud.telemetry.TelemetryCollector;
import com.azure.spring.integration.servicebus.factory.DefaultServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.factory.ServiceBusConnectionStringProvider;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
import com.microsoft.azure.servicebus.QueueClient;
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
 * An auto-configuration for Service Bus Queue.
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureServiceBusAutoConfiguration.class)
@ConditionalOnClass(value = { QueueClient.class, ServiceBusQueueClientFactory.class })
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
public class AzureServiceBusQueueAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusQueueAutoConfiguration.class);

    private static final String SERVICE_BUS_QUEUE = "ServiceBusQueue";
    private static final String NAMESPACE = "Namespace";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_QUEUE);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusNamespaceManager.class)
    public ServiceBusQueueManager serviceBusQueueManager(AzureProperties azureProperties) {
        return new ServiceBusQueueManager(azureProperties);
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

        DefaultServiceBusQueueClientFactory clientFactory = new DefaultServiceBusQueueClientFactory(connectionString);
        clientFactory.setNamespace(properties.getNamespace());
        clientFactory.setServiceBusNamespaceManager(namespaceManager);
        clientFactory.setServiceBusQueueManager(queueManager);

        TelemetryCollector.getInstance().addProperty(SERVICE_BUS_QUEUE, NAMESPACE, getNamespace(connectionString));

        return clientFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusQueueClientFactory.class)
    public ServiceBusQueueOperation queueOperation(ServiceBusQueueClientFactory factory) {
        return new ServiceBusQueueTemplate(factory);
    }


}
