// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureServicePropertiesUtils;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.autoconfigure.storage.AzureStorageConfiguration;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.storage.queue.QueueServiceClientBuilderFactory;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Storage Queue support.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass(QueueServiceClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.storage.queue.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefixes ={ "spring.cloud.azure.storage.queue", "spring.cloud.azure.storage" }, name = { "account-name", "endpoint", "connection-string" })
@Import(AzureStorageConfiguration.class)
public class AzureStorageQueueAutoConfiguration {

    @Bean
    @ConfigurationProperties(AzureStorageQueueProperties.PREFIX)
    AzureStorageQueueProperties azureStorageQueueProperties(AzureStorageGlobalProperties storageGlobalProperties) {
        return AzureServicePropertiesUtils.loadStorageProperties(storageGlobalProperties, new AzureStorageQueueProperties());
    }

    /**
     * Autoconfigure the {@link QueueServiceClient} instance.
     * @param builder the {@link QueueServiceClientBuilder} to build the instance.
     * @return the queue service client.
     */
    @Bean
    @ConditionalOnMissingBean
    public QueueServiceClient queueServiceClient(QueueServiceClientBuilder builder) {
        return builder.buildClient();
    }

    /**
     * Autoconfigure the {@link QueueServiceAsyncClient} instance.
     * @param builder the {@link QueueServiceClientBuilder} to build the instance.
     * @return the queue service async client.
     */
    @Bean
    @ConditionalOnMissingBean
    public QueueServiceAsyncClient queueServiceAsyncClient(QueueServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageQueueProperties.PREFIX, name = "queue-name")
    QueueClient queueClient(QueueServiceClient queueServiceClient, AzureStorageQueueProperties properties) {
        return queueServiceClient.getQueueClient(properties.getQueueName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageQueueProperties.PREFIX, name = "queue-name")
    QueueAsyncClient queueAsyncClient(QueueServiceAsyncClient queueServiceAsyncClient,
                                      AzureStorageQueueProperties properties) {
        return queueServiceAsyncClient.getQueueAsyncClient(properties.getQueueName());
    }

    @Bean
    @ConditionalOnMissingBean
    QueueServiceClientBuilderFactory queueServiceClientBuilderFactory(
        AzureStorageQueueProperties properties,
        ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.StorageQueue>> connectionStringProviders,
        ObjectProvider<AzureServiceClientBuilderCustomizer<QueueServiceClientBuilder>> customizers) {

        final QueueServiceClientBuilderFactory factory = new QueueServiceClientBuilderFactory(properties);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_STORAGE_QUEUE);
        connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    QueueServiceClientBuilder queueServiceClientBuilder(QueueServiceClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnAnyProperty(prefixes = { AzureStorageQueueProperties.PREFIX, AzureStorageGlobalProperties.PREFIX }, name = { "connection-string" })
    StaticConnectionStringProvider<AzureServiceType.StorageQueue> staticStorageQueueConnectionStringProvider(
        AzureStorageQueueProperties storageQueueProperties) {

        return new StaticConnectionStringProvider<>(AzureServiceType.STORAGE_QUEUE,
                                                    storageQueueProperties.getConnectionString());
    }

}
