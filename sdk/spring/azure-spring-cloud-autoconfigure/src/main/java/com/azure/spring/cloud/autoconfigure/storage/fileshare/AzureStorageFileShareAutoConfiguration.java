// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.autoconfigure.storage.resource.AzureStorageFileProtocolResolver;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link ShareServiceClientBuilder} and file share service clients.
 */
@ConditionalOnClass(ShareServiceClientBuilder.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.storage.fileshare", name = "enabled", matchIfMissing = true)
public class AzureStorageFileShareAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureStorageFileShareAutoConfiguration(AzureConfigurationProperties azureProperties) {
        super(azureProperties);
    }

    @Bean
    @ConfigurationProperties(AzureStorageFileShareProperties.PREFIX)
    public AzureStorageFileShareProperties storageFileShareProperties() {
        return copyProperties(this.azureProperties, new AzureStorageFileShareProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClient blobClient(ShareServiceClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceAsyncClient blobAsyncClient(ShareServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClientBuilderFactory factory(AzureStorageFileShareProperties properties) {
        return new ShareServiceClientBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClientBuilder shareServiceClientBuilder(ShareServiceClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public AzureStorageFileProtocolResolver azureStorageFileProtocolResolver(ShareServiceClient shareServiceClient) {
        return new AzureStorageFileProtocolResolver(shareServiceClient);
    }

}
