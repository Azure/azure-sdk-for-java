// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.core.management.AzureEnvironment;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureContextProperties;
import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.cloud.context.core.impl.EventHubNamespaceManager;
import com.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.factory.DefaultEventHubClientFactory;
import com.azure.spring.integration.eventhub.factory.EventHubConnectionStringProvider;
import com.azure.spring.integration.eventhub.impl.EventHubTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureEventHubAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AzureResourceManager.class)
    public EventHubNamespaceManager eventHubNamespaceManager(AzureResourceManager azureResourceManager,
                                                             AzureContextProperties azureContextProperties) {
        return new EventHubNamespaceManager(azureResourceManager, azureContextProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AzureResourceManager.class)
    public StorageAccountManager storageAccountManager(AzureResourceManager azureResourceManager,
                                                       AzureContextProperties azureContextProperties) {
        return new StorageAccountManager(azureResourceManager, azureContextProperties);
    }

    /**
     * Create a {@link EventHubConnectionStringProvider} bean. The bean will hold the connection string to the eventhub.
     * If connection-string property is configured in the property files, it will use it. Otherwise, it will try to
     * construct the connection-string using the resource manager.
     *
     * @param namespaceManager The resource manager for Event Hubs namespaces.
     * @param properties The Event Hubs properties.
     * @return The {@link EventHubConnectionStringProvider} bean.
     * @throws IllegalArgumentException If connection string is empty.
     */
    @Bean
    @ConditionalOnMissingBean
    public EventHubConnectionStringProvider eventHubConnectionStringProvider(
        @Autowired(required = false) EventHubNamespaceManager namespaceManager,
        AzureEventHubProperties properties) {

        final String namespace = properties.getNamespace();
        final String connectionString = properties.getConnectionString();

        if (StringUtils.hasText(connectionString)) {
            return new EventHubConnectionStringProvider(connectionString);
        } else if (namespaceManager != null && StringUtils.hasText(namespace)) {
            return new EventHubConnectionStringProvider(namespaceManager.getOrCreate(namespace));
        }

        LOGGER.warn("Can't construct the EventHubConnectionStringProvider, namespace: {}, connectionString: {}",
            namespace, connectionString);
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientFactory eventhubClientFactory(
        @Autowired(required = false) EnvironmentProvider environmentProvider,
        @Autowired(required = false) StorageAccountManager storageAccountManager,
        EventHubConnectionStringProvider eventHubConnectionStringProvider,
        AzureEventHubProperties properties
    ) {
        if (eventHubConnectionStringProvider == null) {
            LOGGER.info("No event hub connection string provided.");
            return null;
        }

        final String eventHubConnectionString = eventHubConnectionStringProvider.getConnectionString();
        final String storageConnectionString = getStorageConnectionString(properties,
            storageAccountManager,
            environmentProvider == null ? null : environmentProvider.getEnvironment());

        return new DefaultEventHubClientFactory(eventHubConnectionString, storageConnectionString,
            properties.getCheckpointContainer());
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubOperation eventHubOperation(EventHubClientFactory clientFactory) {
        return new EventHubTemplate(clientFactory);
    }


    private String getStorageConnectionString(AzureEventHubProperties properties,
                                              StorageAccountManager storageAccountManager,
                                              AzureEnvironment azureEnvironment) {

        final String accountName = properties.getCheckpointStorageAccount();
        final String accountKey = properties.getCheckpointAccessKey();
        final StorageConnectionStringProvider provider;

        if (storageAccountManager != null) {
            provider = new StorageConnectionStringProvider(storageAccountManager.getOrCreate(accountName));
        } else {
            provider = new StorageConnectionStringProvider(accountName, accountKey, azureEnvironment);
        }

        return provider.getConnectionString();
    }

}
