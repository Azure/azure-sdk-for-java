// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.core.management.AzureEnvironment;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.cloud.context.core.impl.DefaultEnvironmentProvider;
import com.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.azure.spring.cloud.telemetry.TelemetryCollector;
import com.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.azure.spring.integration.storage.queue.StorageQueueTemplate;
import com.azure.spring.integration.storage.queue.factory.DefaultStorageQueueClientFactory;
import com.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
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

import javax.annotation.PostConstruct;
import java.util.Optional;

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
    @ConditionalOnMissingBean({ StorageQueueClientFactory.class, StorageAccountManager.class })
    StorageQueueClientFactory storageQueueClientFactory(
        AzureStorageProperties storageProperties,
        @Autowired(required = false) EnvironmentProvider environmentProvider) {

        final String accountName = storageProperties.getAccount();
        final String accessKey = storageProperties.getAccessKey();

        final AzureEnvironment azureEnvironment = Optional.ofNullable(environmentProvider)
                                                          .orElse(new DefaultEnvironmentProvider())
                                                          .getEnvironment();


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
