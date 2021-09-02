// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for a {@link BlobClientBuilder} and blob service clients.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(BlobClientBuilder.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.storage.blob", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureStorageBlobProperties.class)
@AutoConfigureAfter
public class AzureStorageBlobClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClient blobClient(BlobServiceClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceAsyncClient blobAsyncClient(BlobServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClientBuilderFactory factory(AzureStorageBlobProperties properties) {
        return new BlobServiceClientBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClientBuilder blobClientBuilder(BlobServiceClientBuilderFactory factory) {
        return factory.build();
    }

}
