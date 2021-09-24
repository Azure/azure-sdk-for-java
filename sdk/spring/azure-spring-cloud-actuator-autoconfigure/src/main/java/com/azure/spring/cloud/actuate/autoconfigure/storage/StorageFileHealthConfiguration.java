// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.storage;

import com.azure.spring.cloud.actuate.storage.StorageFileHealthIndicator;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.AzureStorageFileShareAutoConfiguration;
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
@Configuration
@ConditionalOnClass({ ShareServiceAsyncClient.class, HealthIndicator.class })
@AutoConfigureAfter(AzureStorageFileShareAutoConfiguration.class)
@ConditionalOnBean(ShareServiceAsyncClient.class)
public class StorageFileHealthConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-storage")
    public StorageFileHealthIndicator storageFileHealthIndicator(ShareServiceAsyncClient shareServiceAsyncClient) {
        return new StorageFileHealthIndicator(shareServiceAsyncClient);
    }

}
