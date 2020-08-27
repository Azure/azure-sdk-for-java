// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.factory.DefaultEventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.factory.EventHubConnectionStringProvider;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubTemplate;
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
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(EventHubConsumerAsyncClient.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhub.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureEventHubProperties.class)
public class AzureEventHubAutoConfiguration {
    private static final String EVENT_HUB = "EventHub";
    private static final String NAMESPACE = "Namespace";

    @Autowired(required = false)
    private ResourceManagerProvider resourceManagerProvider;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(EVENT_HUB);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubOperation eventHubOperation(EventHubClientFactory clientFactory) {
        return new EventHubTemplate(clientFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubConnectionStringProvider eventHubConnectionStringProvider(
        AzureEventHubProperties eventHubProperties) {
        if (resourceManagerProvider != null) {
            EventHubNamespace namespace = resourceManagerProvider.getEventHubNamespaceManager()
                .getOrCreate(eventHubProperties.getNamespace());
            return new EventHubConnectionStringProvider(namespace);
        } else {
            String connectionString = eventHubProperties.getConnectionString();

            if (!StringUtils.hasText(connectionString)) {
                throw new IllegalArgumentException("Event hubs connection string cannot be empty");
            }

            TelemetryCollector.getInstance()
                .addProperty(EVENT_HUB, NAMESPACE, EventHubUtils.getNamespace(connectionString));
            return new EventHubConnectionStringProvider(connectionString);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientFactory clientFactory(EventHubConnectionStringProvider connectionStringProvider,
                                               AzureEventHubProperties eventHubProperties, EnvironmentProvider environmentProvider) {
        String checkpointConnectionString;
        if (resourceManagerProvider != null) {
            StorageAccount checkpointStorageAccount = resourceManagerProvider.getStorageAccountManager().getOrCreate(
                eventHubProperties.getCheckpointStorageAccount());
            checkpointConnectionString = StorageConnectionStringProvider
                .getConnectionString(checkpointStorageAccount, environmentProvider.getEnvironment());
        } else {
            checkpointConnectionString = StorageConnectionStringProvider
                .getConnectionString(eventHubProperties.getCheckpointStorageAccount(),
                    eventHubProperties.getCheckpointAccessKey(), environmentProvider.getEnvironment());
        }

        return new DefaultEventHubClientFactory(connectionStringProvider, checkpointConnectionString,
            eventHubProperties.getCheckpointContainer());
    }
}
