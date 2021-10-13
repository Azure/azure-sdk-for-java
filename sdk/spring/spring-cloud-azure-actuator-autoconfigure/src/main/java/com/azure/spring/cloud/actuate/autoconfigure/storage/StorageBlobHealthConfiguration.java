// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.storage;

import com.azure.spring.cloud.actuate.storage.StorageBlobHealthIndicator;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.storage.blob.BlobServiceAsyncClient;
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
@ConditionalOnClass({ BlobServiceAsyncClient.class, HealthIndicator.class })
@ConditionalOnBean(BlobServiceAsyncClient.class)
@AutoConfigureAfter(AzureStorageBlobAutoConfiguration.class)
public class StorageBlobHealthConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-storage")
    @ConditionalOnBean(BlobServiceAsyncClient.class)
    public StorageBlobHealthIndicator storageBlobHealthIndicator(BlobServiceAsyncClient blobServiceAsyncClient) {
        return new StorageBlobHealthIndicator(blobServiceAsyncClient);
    }

}
