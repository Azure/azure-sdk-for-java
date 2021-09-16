// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.core.management.AzureEnvironment;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.spring.cloud.autoconfigure.context.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.azure.spring.storage.queue.core.StorageQueueOperation;
import com.azure.spring.storage.queue.core.StorageQueueTemplate;
import com.azure.spring.storage.queue.core.DefaultStorageQueueClientFactory;
import com.azure.spring.storage.queue.core.StorageQueueClientFactory;
import com.azure.storage.queue.QueueServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Auto-configuration class for Azure Storage Queue.
 */
@Configuration
@AutoConfigureAfter({ AzureResourceManagerAutoConfiguration.class})
@ConditionalOnClass({ QueueServiceClient.class, StorageQueueClientFactory.class })
@ConditionalOnProperty(name = "spring.cloud.azure.storage.account")
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageQueueAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({ StorageQueueClientFactory.class, StorageAccountManager.class })
    StorageQueueClientFactory storageQueueClientFactory(
        AzureStorageProperties storageProperties,
        @Autowired(required = false) AzureEnvironment azureEnvironment1) {

        final String accountName = storageProperties.getAccount();
        final String accessKey = storageProperties.getAccessKey();

        final AzureEnvironment azureEnvironment = Optional.ofNullable(azureEnvironment1)
                                                          .orElse(AzureEnvironment.AZURE);


        final String connectionString = new StorageConnectionStringProvider(accountName, accessKey, azureEnvironment)
            .getConnectionString();

        return new DefaultStorageQueueClientFactory(connectionString);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(StorageAccountManager.class)
    StorageQueueClientFactory storageQueueClientFactory(AzureStorageProperties storageProperties,
                                                        StorageAccountManager storageAccountManager) {

        final String accountName = storageProperties.getAccount();

        final StorageAccount storageAccount = storageAccountManager.getOrCreate(accountName);
        final String connectionString = new StorageConnectionStringProvider(storageAccount).getConnectionString();

        return new DefaultStorageQueueClientFactory(connectionString);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(StorageQueueClientFactory.class)
    StorageQueueOperation storageQueueOperation(StorageQueueClientFactory storageQueueClientFactory) {
        return new StorageQueueTemplate(storageQueueClientFactory);
    }
}
