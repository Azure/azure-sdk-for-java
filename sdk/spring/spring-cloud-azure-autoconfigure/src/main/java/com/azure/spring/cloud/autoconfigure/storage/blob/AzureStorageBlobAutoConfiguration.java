// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.factory.Helpers;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.STORAGE_BLOB_SERVICE_CLIENT_BUILDER_BEAN_NAME;

/**
 * Auto-configuration for a {@link BlobServiceClientBuilder} and blob service clients.
 */
@ConditionalOnClass(BlobServiceClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.storage.blob.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.storage.blob", name = { "account-name", "endpoint", "connection-string" })
public class AzureStorageBlobAutoConfiguration {

    public static final String PREFIX = "spring.cloud.azure.storage.blob";

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = PREFIX, name = "blob-name")
    public BlobAsyncClient blobAsyncClient(BlobClientBuilder blobBuilder) {
        return blobBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = PREFIX, name = "container-name")
    public BlobContainerAsyncClient blobContainerAsyncClient(BlobContainerClientBuilder containerClientBuilder) {
        return containerClientBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceAsyncClient blobServiceAsyncClient(
        @Qualifier(STORAGE_BLOB_SERVICE_CLIENT_BUILDER_BEAN_NAME) BlobServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = PREFIX, name = "blob-name")
    public BlobClient blobClient(BlobClientBuilder blobBuilder) {
        return blobBuilder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = PREFIX, name = "container-name")
    public BlobContainerClient blobContainerClient(BlobContainerClientBuilder containerClientBuilder) {
        return containerClientBuilder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClient blobServiceClient(
        @Qualifier(STORAGE_BLOB_SERVICE_CLIENT_BUILDER_BEAN_NAME) BlobServiceClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean("STORAGE_BLOB_CONTAINER_CLIENT_BUILDER_BEAN_NAME")
    @ConditionalOnMissingBean(name = "STORAGE_BLOB_CONTAINER_CLIENT_BUILDER_BEAN_NAME")
    BlobContainerClientBuilder blobContainerClientBuilder() {
        return new BlobContainerClientBuilder();
    }

    @Bean("STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME")
    @ConditionalOnMissingBean(name = "STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME")
    BlobClientBuilder blobClientBuilder() {
        return new BlobClientBuilder();
    }

    @Bean(STORAGE_BLOB_SERVICE_CLIENT_BUILDER_BEAN_NAME)
    @ConditionalOnMissingBean(name = STORAGE_BLOB_SERVICE_CLIENT_BUILDER_BEAN_NAME)
    BlobServiceClientBuilder blobServiceClientBuilder(ConfigurationBuilder configurationBuilder,
                                                      @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                                      Optional<AzureServiceClientBuilderCustomizer<BlobServiceClientBuilder>> builderCustomizer) {
        return Helpers.configureBuilder(
            new BlobServiceClientBuilder(),
            configurationBuilder.section("storage.blob").build(),
            defaultTokenCredential,
            builderCustomizer);
    }
}
