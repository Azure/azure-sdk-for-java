// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.storage;

import com.azure.spring.cloud.actuator.storage.StorageQueueHealthIndicator;
import com.azure.spring.cloud.autoconfigure.storage.queue.AzureStorageQueueAutoConfiguration;
import com.azure.storage.queue.QueueServiceAsyncClient;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration class for Storage actuator.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ QueueServiceAsyncClient.class, HealthIndicator.class })
@ConditionalOnBean(QueueServiceAsyncClient.class)
@AutoConfigureAfter(AzureStorageQueueAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("azure-storage")
public class StorageQueueHealthConfiguration {

    @Bean
    StorageQueueHealthIndicator storageQueueHealthIndicator(QueueServiceAsyncClient queueServiceAsyncClient) {
        return new StorageQueueHealthIndicator(queueServiceAsyncClient);
    }

}
