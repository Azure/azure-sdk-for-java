// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import com.azure.storage.queue.QueueServiceClient;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueTemplate;
import com.microsoft.azure.spring.integration.storage.queue.factory.DefaultStorageQueueClientFactory;
import com.microsoft.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@AutoConfigureAfter({AzureContextAutoConfiguration.class, AzureEnvironmentAutoConfiguration.class})
@ConditionalOnClass({QueueServiceClient.class, StorageQueueClientFactory.class})
@ConditionalOnProperty(name = "spring.cloud.azure.storage.account")
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageQueueAutoConfiguration {

    private static final String STORAGE_QUEUE = "StorageQueue";
    private static final String STORAGE = "Storage";
    private static final String ACCOUNT_NAME = "AccountName";

    @Autowired(required = false)
    private ResourceManagerProvider resourceManagerProvider;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(STORAGE_QUEUE);
    }

    @Bean
    @ConditionalOnMissingBean
    StorageQueueClientFactory storageQueueClientFactory(AzureStorageProperties storageProperties,
                                                        EnvironmentProvider environmentProvider) {

        String connectionString;

        if (resourceManagerProvider != null) {
            String accountName = storageProperties.getAccount();

            StorageAccount storageAccount = resourceManagerProvider.getStorageAccountManager().getOrCreate(accountName);

            connectionString = StorageConnectionStringProvider
                .getConnectionString(storageAccount, environmentProvider.getEnvironment(),
                    storageProperties.isSecureTransfer());
        } else {
            connectionString = StorageConnectionStringProvider
                .getConnectionString(storageProperties.getAccount(), storageProperties.getAccessKey(),
                    environmentProvider.getEnvironment());
        }

        return new DefaultStorageQueueClientFactory(connectionString);
    }

    @Bean
    @ConditionalOnMissingBean
    StorageQueueOperation storageQueueOperation(StorageQueueClientFactory storageQueueClientFactory) {
        return new StorageQueueTemplate(storageQueueClientFactory);
    }
}
