// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.storage.file.share.ShareAsyncClient;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link ShareServiceClientBuilder} and file share service clients.
 */
@ConditionalOnClass(ShareServiceClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.storage.fileshare.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.storage.fileshare", name = { "account-name", "endpoint", "connection-string" })
public class AzureStorageFileShareAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureStorageFileShareAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureStorageFileShareProperties.PREFIX)
    public AzureStorageFileShareProperties azureStorageFileShareProperties() {
        return loadProperties(this.azureGlobalProperties, new AzureStorageFileShareProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClient shareServiceClient(ShareServiceClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceAsyncClient shareServiceAsyncClient(ShareServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClientBuilderFactory shareServiceClientBuilderFactory(AzureStorageFileShareProperties properties) {
        return new ShareServiceClientBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClientBuilder shareServiceClientBuilder(ShareServiceClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "file-name")
    public ShareFileAsyncClient shareFileAsyncClient(AzureStorageFileShareProperties properties,
                                                ShareAsyncClient shareAsyncClient) {
        return shareAsyncClient.getFileClient(properties.getFileName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "share-name")
    public ShareAsyncClient shareAsyncClient(AzureStorageFileShareProperties properties,
                                                     ShareServiceAsyncClient shareServiceAsyncClient) {
        return shareServiceAsyncClient.getShareAsyncClient(properties.getShareName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "file-name")
    public ShareFileClient shareFileClient(AzureStorageFileShareProperties properties,
                                           ShareClient shareClient) {
        return shareClient.getFileClient(properties.getFileName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "share-name")
    public ShareClient shareClient(AzureStorageFileShareProperties properties,
                                                     ShareServiceClient shareServiceClient) {
        return shareServiceClient.getShareClient(properties.getShareName());
    }


}
