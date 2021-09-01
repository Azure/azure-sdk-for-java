// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage;

import com.azure.spring.autoconfigure.storage.actuator.BlobStorageHealthIndicator;
import com.azure.spring.autoconfigure.storage.actuator.FileStorageHealthIndicator;
import com.azure.storage.blob.BlobServiceClientBuilder;
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
@ConditionalOnClass({ BlobServiceClientBuilder.class, ShareServiceClientBuilder.class, HealthIndicator.class })
@AutoConfigureAfter(StorageAutoConfiguration.class)
public class StorageHealthConfiguration {

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-storage")
    @ConditionalOnBean(BlobServiceClientBuilder.class)
    public BlobStorageHealthIndicator blobStorageHealthIndicator(BlobServiceClientBuilder blobServiceClientBuilder) {
        return new BlobStorageHealthIndicator(blobServiceClientBuilder);
    }

    @Bean
    @ConditionalOnEnabledHealthIndicator("azure-storage")
    @ConditionalOnBean(ShareServiceClientBuilder.class)
    public FileStorageHealthIndicator fileStorageHealthIndicator(ShareServiceClientBuilder shareServiceClientBuilder) {
        return new FileStorageHealthIndicator(shareServiceClientBuilder);
    }

}
