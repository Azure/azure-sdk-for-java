// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.storage;

import com.azure.spring.cloud.actuate.storage.StorageFileHealthIndicator;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.AzureStorageFileShareAutoConfiguration;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
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
@ConditionalOnClass({ ShareServiceClientBuilder.class, HealthIndicator.class })
@AutoConfigureAfter(AzureStorageFileShareAutoConfiguration.class)
public class StorageFileHealthConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-storage")
    @ConditionalOnBean(ShareServiceAsyncClient.class)
    public StorageFileHealthIndicator fileStorageHealthIndicator(ShareServiceAsyncClient shareServiceAsyncClient) {
        return new StorageFileHealthIndicator(shareServiceAsyncClient);
    }

}
