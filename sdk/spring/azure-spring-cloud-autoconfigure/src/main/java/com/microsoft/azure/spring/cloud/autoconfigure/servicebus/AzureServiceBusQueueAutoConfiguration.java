// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.integration.servicebus.factory.DefaultServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
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
@ConditionalOnClass(value = {QueueClient.class, ServiceBusQueueClientFactory.class})
@ConditionalOnProperty(value = "spring.cloud.azure.servicebus.enabled", matchIfMissing = true)
public class AzureServiceBusQueueAutoConfiguration {
    private static final String SERVICE_BUS_QUEUE = "ServiceBusQueue";
    private static final String NAMESPACE = "Namespace";

    @Autowired(required = false)
    private ResourceManagerProvider resourceManagerProvider;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(SERVICE_BUS_QUEUE);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusQueueClientFactory queueClientFactory(AzureServiceBusProperties serviceBusProperties) {
        String connectionString = serviceBusProperties.getConnectionString();
        DefaultServiceBusQueueClientFactory clientFactory =
            new DefaultServiceBusQueueClientFactory(serviceBusProperties.getConnectionString());

        if (resourceManagerProvider != null) {
            clientFactory.setResourceManagerProvider(resourceManagerProvider);
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
}
