// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.autoconfigure.storage.resource.AzureStorageBlobProtocolResolver;
import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link BlobClientBuilder} and blob service clients.
 */
@ConditionalOnClass(BlobClientBuilder.class)
@ConditionalOnProperty(prefix = AzureStorageBlobProperties.PREFIX, name = "enabled", matchIfMissing = true)
@ConditionalOnBean(AzureConfigurationProperties.class)
public class AzureStorageBlobAutoConfiguration extends AzureServiceConfigurationBase {


    public AzureStorageBlobAutoConfiguration(AzureConfigurationProperties azureProperties) {
        super(azureProperties);
    }

    @Bean
    @ConfigurationProperties(AzureStorageBlobProperties.PREFIX)
    public AzureStorageBlobProperties storageBlobProperties() {
        return loadProperties(this.azureProperties, new AzureStorageBlobProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageBlobProperties.PREFIX, name = "blob-name")
    public BlobAsyncClient blobAsyncClient(AzureStorageBlobProperties properties,
                                           BlobContainerAsyncClient blobContainerAsyncClient) {
        return blobContainerAsyncClient.getBlobAsyncClient(properties.getBlobName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageBlobProperties.PREFIX, name = "container-name")
    public BlobContainerAsyncClient blobContainerAsyncClient(AzureStorageBlobProperties properties,
                                                             BlobServiceAsyncClient blobServiceAsyncClient) {
        return blobServiceAsyncClient.getBlobContainerAsyncClient(properties.getContainerName());
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceAsyncClient blobServiceAsyncClient(BlobServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageBlobProperties.PREFIX, name = "blob-name")
    public BlobClient blobClient(AzureStorageBlobProperties properties,
                                 BlobContainerClient blobContainerClient) {
        return blobContainerClient.getBlobClient(properties.getBlobName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageBlobProperties.PREFIX, name = "container-name")
    public BlobContainerClient blobContainerClient(AzureStorageBlobProperties properties,
                                                   BlobServiceClient blobServiceClient) {
        return blobServiceClient.getBlobContainerClient(properties.getContainerName());
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClient blobServiceClient(BlobServiceClientBuilder builder) {
        return builder.buildClient();
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

    @Bean
    @ConditionalOnBean(BlobServiceClient.class)
    public AzureStorageBlobProtocolResolver azureStorageBlobProtocolResolver(BlobServiceClient blobServiceClient) {
        return new AzureStorageBlobProtocolResolver(blobServiceClient);
    }

}
