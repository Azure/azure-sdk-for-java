// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import javax.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.storage.queue.QueueServiceClient;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.microsoft.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueTemplate;
import com.microsoft.azure.spring.integration.storage.queue.factory.DefaultStorageQueueClientFactory;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;

@Configuration
@AutoConfigureAfter({ AzureContextAutoConfiguration.class, AzureEnvironmentAutoConfiguration.class,
        AzureStorageAutoConfiguration.class })
@ConditionalOnClass({ QueueServiceClient.class, StorageQueueClientFactory.class })
@ConditionalOnProperty(name = "spring.cloud.azure.storage.account")
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageQueueAutoConfiguration {

    private static final String STORAGE_QUEUE = "StorageQueue";
    private static final String STORAGE = "Storage";
    private static final String ACCOUNT_NAME = "AccountName";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(STORAGE_QUEUE);
    }

    @Bean
    @ConditionalOnMissingBean
    StorageQueueClientFactory storageQueueClientFactory(AzureStorageProperties storageProperties,
            StorageAccountManager storageAccountManager, EnvironmentProvider environmentProvider) {

        String connectionString;

        String accountName = storageProperties.getAccount();

        StorageAccount storageAccount = storageAccountManager.getOrCreate(accountName);

        connectionString = StorageConnectionStringProvider.getConnectionString(storageAccount,
                environmentProvider.getEnvironment(), storageProperties.isSecureTransfer());

        return new DefaultStorageQueueClientFactory(connectionString);
    }

    @Bean
    @ConditionalOnMissingBean
    StorageQueueOperation storageQueueOperation(StorageQueueClientFactory storageQueueClientFactory) {
        return new StorageQueueTemplate(storageQueueClientFactory);
    }
}
