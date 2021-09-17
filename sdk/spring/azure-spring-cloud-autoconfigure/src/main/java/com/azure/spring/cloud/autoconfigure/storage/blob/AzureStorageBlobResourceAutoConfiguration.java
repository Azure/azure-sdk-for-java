// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.autoconfigure.storage.resource.AzureStorageBlobProtocolResolver;
import com.azure.storage.blob.BlobServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link AzureStorageBlobProtocolResolver}.
 */
@ConditionalOnClass({ AzureStorageBlobProtocolResolver.class, BlobServiceClient.class })
@AzureStorageBlobAutoConfiguration.ConditionalOnStorageBlob
public class AzureStorageBlobResourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AzureStorageBlobProtocolResolver azureStorageBlobProtocolResolver() {
        return new AzureStorageBlobProtocolResolver();
    }

}
