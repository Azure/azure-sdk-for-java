// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.ConnectionStringProvider;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Auto-configuration for a {@link QueueServiceClientBuilder} and queue service clients.
 */
@ConditionalOnClass(QueueServiceClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.storage.queue.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.storage.queue", name = { "account-name", "endpoint", "connection-string" })
public class AzureStorageQueueAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureStorageQueueAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureStorageQueueProperties.PREFIX)
    public AzureStorageQueueProperties azureStorageQueueProperties() {
        return loadProperties(this.azureGlobalProperties, new AzureStorageQueueProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueServiceClient queueServiceClient(QueueServiceClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueServiceAsyncClient queueServiceAsyncClient(QueueServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueServiceClientBuilderFactory queueServiceClientBuilderFactory(AzureStorageQueueProperties properties,
                                                                             ObjectProvider<ConnectionStringProvider<AzureServiceType.StorageQueue>> connectionStringProviders) {
        final QueueServiceClientBuilderFactory factory = new QueueServiceClientBuilderFactory(properties);
        factory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueServiceClientBuilder queueServiceClientBuilder(QueueServiceClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    public StaticConnectionStringProvider<AzureServiceType.StorageQueue> staticStorageQueueConnectionStringProvider(
        AzureStorageQueueProperties storageQueueProperties) {

        return new StaticConnectionStringProvider<>(AzureServiceType.STORAGE_QUEUE,
                                                    storageQueueProperties.getConnectionString());
    }

}
