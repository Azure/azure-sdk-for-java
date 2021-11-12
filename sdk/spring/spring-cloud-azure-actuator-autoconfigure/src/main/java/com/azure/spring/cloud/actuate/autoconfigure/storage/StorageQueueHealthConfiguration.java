// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.storage;

import com.azure.spring.cloud.actuate.storage.StorageQueueHealthIndicator;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.storage.queue.AzureStorageQueueAutoConfiguration;
import com.azure.storage.queue.QueueServiceAsyncClient;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration class for Storage actuator.
 */
@Configuration
@ConditionalOnClass({ QueueServiceAsyncClient.class, HealthIndicator.class })
@ConditionalOnBean(QueueServiceAsyncClient.class)
@AutoConfigureAfter(AzureStorageQueueAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("azure-storage")
@ConditionalOnProperty(value = "spring.cloud.azure.storage.queue.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.storage.queue", name = { "account-name", "endpoint", "connection-string" })
public class StorageQueueHealthConfiguration {

    @Bean
    @ConditionalOnBean(QueueServiceAsyncClient.class)
    public StorageQueueHealthIndicator storageQueueHealthIndicator(QueueServiceAsyncClient queueServiceAsyncClient) {
        return new StorageQueueHealthIndicator(queueServiceAsyncClient);
    }

}
