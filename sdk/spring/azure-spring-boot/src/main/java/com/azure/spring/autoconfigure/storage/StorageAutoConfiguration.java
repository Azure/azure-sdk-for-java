// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.spring.autoconfigure.storage.resource.AzureStorageProtocolResolver;
import com.azure.spring.utils.ApplicationId;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * An auto-configuration for Azure Storage Account
 *
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnClass({ BlobServiceClientBuilder.class, ShareServiceClientBuilder.class })
@ConditionalOnResource(resources = "classpath:storage.enable.config")
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("azure.storage.blob-endpoint")
    public BlobServiceClientBuilder blobServiceClientBuilder(StorageProperties storageProperties) {
        final String accountName = storageProperties.getAccountName();
        final String accountKey = storageProperties.getAccountKey();

        return new BlobServiceClientBuilder()
            .endpoint(storageProperties.getBlobEndpoint())
            .credential(new StorageSharedKeyCredential(accountName, accountKey))
            .httpLogOptions(new HttpLogOptions().setApplicationId(ApplicationId.AZURE_SPRING_STORAGE_BLOB));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("azure.storage.file-endpoint")
    public ShareServiceClientBuilder shareServiceClientBuilder(StorageProperties storageProperties) {
        final String accountName = storageProperties.getAccountName();
        final String accountKey = storageProperties.getAccountKey();

        return new ShareServiceClientBuilder()
            .endpoint(storageProperties.getFileEndpoint())
            .credential(new StorageSharedKeyCredential(accountName, accountKey))
            .httpLogOptions(new HttpLogOptions().setApplicationId(ApplicationId.AZURE_SPRING_STORAGE_FILES));
    }

    @Configuration
    @ConditionalOnClass(AzureStorageProtocolResolver.class)
    @Import(AzureStorageProtocolResolver.class)
    static class StorageResourceConfiguration {
    }
}
