// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.ConnectionStringProvider;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auto-configuration for a {@link QueueServiceClientBuilder} and queue service clients.
 */
@ConditionalOnClass(QueueServiceClientBuilder.class)
@AzureStorageQueueAutoConfiguration.ConditionalOnStorageQueue
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

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression("${spring.cloud.azure.storage.queue.enabled:true} and ("
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.storage.queue.account-name:}') or "
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.storage.queue.endpoint:}') or "
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.storage.queue.connection-string:}'))")
    public @interface ConditionalOnStorageQueue {
    }

}
