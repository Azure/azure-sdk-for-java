// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.EventHubNamespaceManager;
import com.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.azure.spring.cloud.telemetry.TelemetryCollector;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.factory.DefaultEventHubClientFactory;
import com.azure.spring.integration.eventhub.factory.EventHubConnectionStringProvider;
import com.azure.spring.integration.eventhub.impl.EventHubTemplate;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(name = { "com.microsoft.azure.spring.cloud.autoconfigure.storage.AzureStorageAutoConfiguration",
    "com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration" })
@ConditionalOnClass(EventHubConsumerAsyncClient.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhub.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureEventHubProperties.class)
public class AzureEventHubAutoConfiguration {
    private static final String EVENT_HUB = "EventHub";
    private static final String NAMESPACE = "Namespace";

    @Autowired(required = false)
    private EventHubNamespaceManager eventHubNamespaceManager;

    @Autowired(required = false)
    private StorageAccountManager storageAccountManager;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(EVENT_HUB);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubOperation eventHubOperation(EventHubClientFactory clientFactory) {
        return new EventHubTemplate(clientFactory);
    }

    /**
     * Create a {@link EventHubConnectionStringProvider} bean.
     *
     * @param eventHubProperties The Event Hubs properties.
     * @return The {@link EventHubConnectionStringProvider} bean.
     * @throws IllegalArgumentException If connection string is empty.
     */
    @Bean
    @ConditionalOnMissingBean
    public EventHubConnectionStringProvider eventHubConnectionStringProvider(
        AzureEventHubProperties eventHubProperties) {
        if (eventHubNamespaceManager != null) {
            EventHubNamespace namespace = eventHubNamespaceManager.getOrCreate(eventHubProperties.getNamespace());
            return new EventHubConnectionStringProvider(namespace);
        } else {
            String connectionString = eventHubProperties.getConnectionString();

            if (StringUtils.hasText(connectionString)) {
                TelemetryCollector.getInstance()
                                  .addProperty(EVENT_HUB, NAMESPACE, EventHubUtils.getNamespace(connectionString));
            }
            return new EventHubConnectionStringProvider(connectionString);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientFactory clientFactory(EventHubConnectionStringProvider connectionStringProvider,
                                               AzureEventHubProperties eventHubProperties,
                                               EnvironmentProvider environmentProvider) {
        String checkpointConnectionString;
        if (storageAccountManager != null) {
            StorageAccount checkpointStorageAccount = storageAccountManager
                .getOrCreate(eventHubProperties.getCheckpointStorageAccount());
            checkpointConnectionString = StorageConnectionStringProvider.getConnectionString(checkpointStorageAccount,
                environmentProvider.getEnvironment());
        } else {
            checkpointConnectionString = StorageConnectionStringProvider.getConnectionString(
                eventHubProperties.getCheckpointStorageAccount(), eventHubProperties.getCheckpointAccessKey(),
                environmentProvider.getEnvironment());
        }

        return new DefaultEventHubClientFactory(connectionStringProvider, checkpointConnectionString,
            eventHubProperties.getCheckpointContainer());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("spring.cloud.azure.resource-group")
    public EventHubNamespaceManager eventHubNamespaceManager(Azure azure, AzureProperties azureProperties) {
        return new EventHubNamespaceManager(azure, azureProperties);
    }

}
