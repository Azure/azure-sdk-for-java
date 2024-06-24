// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.queue;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureServicePropertiesUtils;
import com.azure.spring.cloud.autoconfigure.implementation.storage.AzureStorageConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Storage Queue support.
 *
 * @since 4.0.0
 */
@Conditional(AzureStorageQueueAutoConfigurationCondition.class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@Import(AzureStorageConfiguration.class)
public class AzureStorageQueueAutoConfiguration {

    @Bean
    @ConfigurationProperties(AzureStorageQueueProperties.PREFIX)
    AzureStorageQueueProperties azureStorageQueueProperties(@Qualifier("azureStorageProperties") AzureStorageProperties azureStorageProperties) {
        return AzureServicePropertiesUtils.loadServiceCommonProperties(azureStorageProperties, new AzureStorageQueueProperties());
    }

    @Bean
    @ConditionalOnMissingBean(AzureStorageQueueConnectionDetails.class)
    PropertiesAzureStorageQueueConnectionDetails azureStorageQueueConnectionDetails(AzureStorageQueueProperties properties) {
        return new PropertiesAzureStorageQueueConnectionDetails(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    QueueServiceClient queueServiceClient(QueueServiceClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    QueueServiceAsyncClient queueServiceAsyncClient(QueueServiceClientBuilder builder) {
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
        AzureStorageQueueConnectionDetails connectionDetails,
        ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.StorageQueue>> connectionStringProviders,
        ObjectProvider<AzureServiceClientBuilderCustomizer<QueueServiceClientBuilder>> customizers) {
        if (!(connectionDetails instanceof PropertiesAzureStorageQueueConnectionDetails)) {
            properties.setEndpoint(connectionDetails.getEndpoint());
            properties.setConnectionString(connectionDetails.getConnectionString());
        }
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
    @ConditionalOnAnyProperty(prefixes = { AzureStorageQueueProperties.PREFIX, AzureStorageProperties.PREFIX }, name = { "connection-string" })
    StaticConnectionStringProvider<AzureServiceType.StorageQueue> staticStorageQueueConnectionStringProvider(
        AzureStorageQueueConnectionDetails connectionDetails) {

        return new StaticConnectionStringProvider<>(AzureServiceType.STORAGE_QUEUE,
                                                    connectionDetails.getConnectionString());
    }

    static class PropertiesAzureStorageQueueConnectionDetails implements AzureStorageQueueConnectionDetails {

        private final AzureStorageQueueProperties properties;

        PropertiesAzureStorageQueueConnectionDetails(AzureStorageQueueProperties properties) {
            this.properties = properties;
        }

        @Override
        public String getConnectionString() {
            return this.properties.getConnectionString();
        }

        @Override
        public String getEndpoint() {
            return this.properties.getEndpoint();
        }
    }

}
