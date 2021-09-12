// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage;

import com.azure.spring.autoconfigure.storage.resource.AzureStorageFileProtocolResolver;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.AzureStorageFileShareAutoConfiguration;
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
@AutoConfigureAfter({ AzureStorageBlobAutoConfiguration.class, AzureStorageFileShareAutoConfiguration.class })
public class StorageResourceAutoConfiguration {

    // TODO (xiada) move to file autoconfiguration

    @Bean
    @ConditionalOnBean(ShareServiceClient.class)
    public AzureStorageFileProtocolResolver azureStorageFileProtocolResolver(ShareServiceClient shareServiceClient) {
        return new AzureStorageFileProtocolResolver(shareServiceClient);
    }
}
