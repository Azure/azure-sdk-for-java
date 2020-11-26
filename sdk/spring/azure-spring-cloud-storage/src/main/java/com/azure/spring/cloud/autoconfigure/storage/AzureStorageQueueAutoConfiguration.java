// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.azure.spring.cloud.telemetry.TelemetryCollector;
import com.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.azure.spring.integration.storage.queue.StorageQueueTemplate;
import com.azure.spring.integration.storage.queue.factory.DefaultStorageQueueClientFactory;
import com.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import com.azure.storage.queue.QueueServiceClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Auto-configuration class for Azure Storage Queue.
 */
@Configuration
@AutoConfigureAfter({ AzureContextAutoConfiguration.class, AzureEnvironmentAutoConfiguration.class })
@ConditionalOnClass({ QueueServiceClient.class, StorageQueueClientFactory.class })
@ConditionalOnProperty(name = "spring.cloud.azure.storage.account")
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageQueueAutoConfiguration {

    private static final String STORAGE_QUEUE = "StorageQueue";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(STORAGE_QUEUE);
    }

    @Bean
    @ConditionalOnMissingBean
    StorageQueueClientFactory storageQueueClientFactory(AzureStorageProperties storageProperties,
                                                        StorageAccountManager storageAccountManager) {

        final String accountName = storageProperties.getAccount();
        final StorageAccount storageAccount = storageAccountManager.getOrCreate(accountName);
        final String connectionString = new StorageConnectionStringProvider(storageAccount).getConnectionString();

        return new DefaultStorageQueueClientFactory(connectionString);
    }

    @Bean
    @ConditionalOnMissingBean
    StorageQueueOperation storageQueueOperation(StorageQueueClientFactory storageQueueClientFactory) {
        return new StorageQueueTemplate(storageQueueClientFactory);
    }
}
