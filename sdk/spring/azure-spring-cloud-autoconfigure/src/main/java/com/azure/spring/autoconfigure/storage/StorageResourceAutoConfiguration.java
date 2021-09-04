// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage;

import com.azure.spring.autoconfigure.storage.resource.AzureStorageBlobProtocolResolver;
import com.azure.spring.autoconfigure.storage.resource.AzureStorageFileProtocolResolver;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobClientAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.AzureStorageFileShareClientAutoConfiguration;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.file.share.ShareServiceClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * An auto-configuration for Azure Storage Account
 *
 * @author Warren Zhu
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({ AzureStorageBlobClientAutoConfiguration.class, AzureStorageFileShareClientAutoConfiguration.class })
public class StorageResourceAutoConfiguration {

    @Bean
    @ConditionalOnBean(BlobServiceClient.class)
    public AzureStorageBlobProtocolResolver azureStorageBlobProtocolResolver(BlobServiceClient blobServiceClient) {
        System.out.println("=====in");
        return new AzureStorageBlobProtocolResolver(blobServiceClient);
    }

    @Bean
    @ConditionalOnBean(ShareServiceClient.class)
    public AzureStorageFileProtocolResolver azureStorageFileProtocolResolver(ShareServiceClient shareServiceClient) {
        System.out.println("=====out");
        return new AzureStorageFileProtocolResolver(shareServiceClient);
    }
}
