// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.service.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * Auto-configuration for a {@link BlobServiceClientBuilder} and blob service clients.
 */
@ConditionalOnClass(BlobServiceClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.storage.blob.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.storage.blob", name = { "account-name", "endpoint", "connection-string" })
public class AzureStorageBlobAutoConfiguration extends AzureServiceConfigurationBase {

    public AzureStorageBlobAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureStorageBlobProperties.PREFIX)
    public AzureStorageBlobProperties azureStorageBlobProperties() {
        return loadProperties(this.azureGlobalProperties, new AzureStorageBlobProperties());
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

    @Bean(STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    public BlobServiceClientBuilderFactory blobServiceClientBuilderFactory(AzureStorageBlobProperties properties) {
        return new BlobServiceClientBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClientBuilder blobServiceClientBuilder(@Qualifier(STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME)
                                                                     BlobServiceClientBuilderFactory factory) {
        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_STORAGE_BLOB);
        return factory.build();
    }

}
