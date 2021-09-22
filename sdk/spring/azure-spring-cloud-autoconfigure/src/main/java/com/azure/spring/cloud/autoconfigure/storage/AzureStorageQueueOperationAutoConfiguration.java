// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.cloud.autoconfigure.storage.queue.AzureStorageQueueAutoConfiguration;
import com.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.azure.spring.integration.storage.queue.StorageQueueTemplate;
import com.azure.spring.integration.storage.queue.factory.DefaultStorageQueueClientFactory;
import com.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import com.azure.storage.queue.QueueServiceAsyncClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration class for Azure Storage Queue.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(StorageQueueClientFactory.class)
@ConditionalOnExpression("${spring.cloud.azure.storage.queue.enabled:true}")
@ConditionalOnBean(QueueServiceAsyncClient.class)
@AutoConfigureAfter(AzureStorageQueueAutoConfiguration.class)
public class AzureStorageQueueOperationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StorageQueueClientFactory storageQueueClientFactory(QueueServiceAsyncClient queueServiceAsyncClient) {
        return new DefaultStorageQueueClientFactory(queueServiceAsyncClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public StorageQueueOperation storageQueueOperation(StorageQueueClientFactory storageQueueClientFactory) {
        return new StorageQueueTemplate(storageQueueClientFactory);
    }
}
