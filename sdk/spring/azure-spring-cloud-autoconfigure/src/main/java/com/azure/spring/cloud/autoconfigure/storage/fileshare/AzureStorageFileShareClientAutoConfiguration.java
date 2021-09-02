// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for a {@link ShareServiceClientBuilder} and file share service clients.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ShareServiceClientBuilder.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.storage.fileshare", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureStorageFileShareProperties.class)
@AutoConfigureAfter
public class AzureStorageFileShareClientAutoConfiguration {

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

}
