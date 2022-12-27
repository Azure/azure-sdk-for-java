// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.queue;

import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.messaging.implementation.converter.ObjectMapperHolder;
import com.azure.spring.messaging.storage.queue.core.StorageQueueTemplate;
import com.azure.spring.messaging.storage.queue.core.factory.StorageQueueClientFactory;
import com.azure.spring.messaging.storage.queue.core.properties.StorageQueueProperties;
import com.azure.spring.messaging.storage.queue.implementation.factory.DefaultStorageQueueClientFactory;
import com.azure.spring.messaging.storage.queue.implementation.support.converter.StorageQueueMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyAzureCommonProperties;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Messaging Azure Storage Queue support.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(StorageQueueClientFactory.class)
@ConditionalOnProperty(value = "spring.cloud.azure.storage.queue.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(AzureStorageQueueProperties.class)
@AutoConfigureAfter(AzureStorageQueueAutoConfiguration.class)
public class AzureStorageQueueMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    StorageQueueClientFactory storageQueueClientFactory(AzureStorageQueueProperties properties) {
        StorageQueueProperties storageQueueProperties = new StorageQueueProperties();
        BeanUtils.copyProperties(properties, storageQueueProperties);
        copyAzureCommonProperties(properties, storageQueueProperties);
        return new DefaultStorageQueueClientFactory(storageQueueProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    StorageQueueTemplate storageQueueTemplate(StorageQueueClientFactory storageQueueClientFactory,
                                                     StorageQueueMessageConverter messageConverter) {
        StorageQueueTemplate storageQueueTemplate = new StorageQueueTemplate(storageQueueClientFactory);
        storageQueueTemplate.setMessageConverter(messageConverter);
        return storageQueueTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.azure.message-converter.isolated-object-mapper", havingValue = "true", matchIfMissing = true)
    StorageQueueMessageConverter defaultStorageQueueMessageConverter() {
        return new StorageQueueMessageConverter(ObjectMapperHolder.OBJECT_MAPPER);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.azure.message-converter.isolated-object-mapper", havingValue = "false")
    StorageQueueMessageConverter storageQueueMessageConverter(ObjectMapper objectMapper) {
        return new StorageQueueMessageConverter(objectMapper);
    }
}
