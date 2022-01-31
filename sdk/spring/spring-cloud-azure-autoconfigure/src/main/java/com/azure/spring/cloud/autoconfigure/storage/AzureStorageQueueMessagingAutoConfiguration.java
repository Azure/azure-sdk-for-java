// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.core.util.ConfigurationBuilder;
import com.azure.spring.cloud.autoconfigure.storage.queue.AzureStorageQueueAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.storage.queue.core.StorageQueueTemplate;
import com.azure.spring.storage.queue.core.factory.DefaultStorageQueueClientFactory;
import com.azure.spring.storage.queue.core.factory.StorageQueueClientFactory;
import com.azure.spring.storage.queue.core.properties.StorageQueueProperties;
import com.azure.spring.storage.queue.support.converter.StorageQueueMessageConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.spring.core.util.AzurePropertiesUtils.copyAzureCommonProperties;

/**
 * Auto-configuration class for Azure Storage Queue.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(StorageQueueClientFactory.class)
@ConditionalOnProperty(value = "spring.cloud.azure.storage.queue.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(AzureStorageQueueProperties.class)
@AutoConfigureAfter(AzureStorageQueueAutoConfiguration.class)
public class AzureStorageQueueMessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StorageQueueClientFactory storageQueueClientFactory(AzureStorageQueueProperties properties, ConfigurationBuilder configurationBuilder) {
        StorageQueueProperties storageQueueProperties = new StorageQueueProperties();
        BeanUtils.copyProperties(properties, storageQueueProperties);
        copyAzureCommonProperties(properties, storageQueueProperties);
        return new DefaultStorageQueueClientFactory(storageQueueProperties, configurationBuilder.section("storage.queue").build());
    }

    @Bean
    @ConditionalOnMissingBean
    public StorageQueueTemplate storageQueueTemplate(StorageQueueClientFactory storageQueueClientFactory,
                                                     StorageQueueMessageConverter messageConverter) {
        StorageQueueTemplate storageQueueTemplate = new StorageQueueTemplate(storageQueueClientFactory);
        storageQueueTemplate.setMessageConverter(messageConverter);
        return storageQueueTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public StorageQueueMessageConverter messageConverter() {
        return new StorageQueueMessageConverter();
    }
}
