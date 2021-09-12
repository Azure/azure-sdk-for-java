// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.storage;

import com.azure.spring.cloud.actuate.storage.StorageQueueHealthIndicator;
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
@Configuration
@ConditionalOnClass({ QueueServiceAsyncClient.class, HealthIndicator.class })
@AutoConfigureAfter(AzureStorageQueueAutoConfiguration.class)
public class StorageQueueHealthConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-storage")
    @ConditionalOnBean(QueueServiceAsyncClient.class)
    public StorageQueueHealthIndicator fileStorageHealthIndicator(QueueServiceAsyncClient queueServiceAsyncClient) {
        return new StorageQueueHealthIndicator(queueServiceAsyncClient);
    }

}
