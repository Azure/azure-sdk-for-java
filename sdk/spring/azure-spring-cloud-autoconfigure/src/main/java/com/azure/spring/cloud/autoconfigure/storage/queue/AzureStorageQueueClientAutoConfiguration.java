// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue;

import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for a {@link QueueServiceClientBuilder} and queue service clients.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(QueueServiceClientBuilder.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.storage.queue", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureStorageQueueProperties.class)
@AutoConfigureAfter
public class AzureStorageQueueClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QueueServiceClient blobClient(QueueServiceClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueServiceAsyncClient blobAsyncClient(QueueServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueServiceClientBuilderFactory factory(AzureStorageQueueProperties properties) {
        return new QueueServiceClientBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueServiceClientBuilder queueClientBuilder(QueueServiceClientBuilderFactory factory) {
        return factory.build();
    }

}
