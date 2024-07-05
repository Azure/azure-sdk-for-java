// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.implementation.storage;

import com.azure.spring.cloud.actuator.implementation.storage.StorageFileShareHealthIndicator;
import com.azure.spring.cloud.autoconfigure.implementation.storage.fileshare.AzureStorageFileShareAutoConfiguration;
import com.azure.storage.file.share.ShareServiceAsyncClient;
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
@ConditionalOnClass({ ShareServiceAsyncClient.class, HealthIndicator.class })
@ConditionalOnBean(ShareServiceAsyncClient.class)
@AutoConfigureAfter(AzureStorageFileShareAutoConfiguration.class)
@ConditionalOnEnabledHealthIndicator("azure-storage-fileshare")
public class StorageFileShareHealthConfiguration {

    @Bean
    StorageFileShareHealthIndicator storageFileShareHealthIndicator(ShareServiceAsyncClient shareServiceAsyncClient) {
        return new StorageFileShareHealthIndicator(shareServiceAsyncClient);
    }
}
