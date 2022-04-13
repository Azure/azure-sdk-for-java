// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.autoconfigure.storage.queue.AzureStorageQueueAutoConfiguration;
import com.azure.spring.messaging.storage.queue.core.StorageQueueTemplate;
import com.azure.spring.messaging.storage.queue.core.factory.StorageQueueClientFactory;
import com.azure.spring.messaging.storage.queue.core.properties.StorageQueueProperties;
import com.azure.spring.messaging.storage.queue.implementation.factory.DefaultStorageQueueClientFactory;
import com.azure.spring.messaging.storage.queue.support.converter.StorageQueueMessageConverter;
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

    /**
     * Autoconfigure the {@link StorageQueueTemplate} instance.
     * @param storageQueueClientFactory the storage queue client factory to create the storage queue clients for the template.
     * @param messageConverter the message converter used by the template.
     * @return the storage queue template.
     */
    @Bean
    @ConditionalOnMissingBean
    public StorageQueueTemplate storageQueueTemplate(StorageQueueClientFactory storageQueueClientFactory,
                                                     StorageQueueMessageConverter messageConverter) {
        StorageQueueTemplate storageQueueTemplate = new StorageQueueTemplate(storageQueueClientFactory);
        storageQueueTemplate.setMessageConverter(messageConverter);
        return storageQueueTemplate;
    }

    /**
     * Autoconfigure the {@link StorageQueueMessageConverter} instance.
     * @return the storage queue message converter.
     */
    @Bean
    @ConditionalOnMissingBean
    public StorageQueueMessageConverter messageConverter() {
        return new StorageQueueMessageConverter();
    }
}
