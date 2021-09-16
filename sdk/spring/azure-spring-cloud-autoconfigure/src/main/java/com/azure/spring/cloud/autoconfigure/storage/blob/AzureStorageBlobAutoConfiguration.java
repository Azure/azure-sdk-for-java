// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auto-configuration for a {@link BlobClientBuilder} and blob service clients.
 */
@ConditionalOnClass(BlobClientBuilder.class)
@AzureStorageBlobAutoConfiguration.ConditionalOnStorageBlob
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

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClientBuilderFactory blobServiceClientBuilderFactory(AzureStorageBlobProperties properties) {
        return new BlobServiceClientBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClientBuilder blobServiceClientBuilder(BlobServiceClientBuilderFactory factory) {
        return factory.build();
    }

    /**
     * Condition indicates when storage blob should be auto-configured.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression("${spring.cloud.azure.storage.blob.enabled:true} and ("
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.storage.blob.account-name:}') or "
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.storage.blob.endpoint:}') or "
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.storage.blob.connection-string:}'))")
    public @interface ConditionalOnStorageBlob {
    }

}
