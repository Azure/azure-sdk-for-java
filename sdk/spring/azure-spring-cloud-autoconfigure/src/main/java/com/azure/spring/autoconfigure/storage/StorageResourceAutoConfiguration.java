// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage;

import com.azure.spring.autoconfigure.storage.resource.AzureStorageProtocolResolver;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobClientAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.AzureStorageFileShareClientAutoConfiguration;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.file.share.ShareServiceClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * An auto-configuration for Azure Storage Account
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter({ AzureStorageBlobClientAutoConfiguration.class, AzureStorageFileShareClientAutoConfiguration.class })
@ConditionalOnClass({ AzureStorageProtocolResolver.class })
@ConditionalOnResource(resources = "classpath:storage.enable.config")
public class StorageResourceAutoConfiguration {

    @Bean
    public AzureStorageProtocolResolver azureStorageProtocolResolver(
        ObjectProvider<BlobServiceClient> blobServiceClients,
        ObjectProvider<ShareServiceClient> shareServiceClients) {

        return new AzureStorageProtocolResolver(blobServiceClients.getIfAvailable(),
                                                shareServiceClients.getIfAvailable());
    }
}
