// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Storage Blob support.
 *
 * @since 4.0.0
 */
@ConditionalOnClass(BlobServiceClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.storage.blob.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.storage.blob", name = { "account-name", "endpoint", "connection-string" })
public class AzureStorageBlobAutoConfiguration extends AzureServiceConfigurationBase {

    AzureStorageBlobAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureStorageBlobProperties.PREFIX)
    AzureStorageBlobProperties azureStorageBlobProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureStorageBlobProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageBlobProperties.PREFIX, name = "blob-name")
    BlobAsyncClient blobAsyncClient(AzureStorageBlobProperties properties,
                                    BlobContainerAsyncClient blobContainerAsyncClient) {
        return blobContainerAsyncClient.getBlobAsyncClient(properties.getBlobName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageBlobProperties.PREFIX, name = "container-name")
    BlobContainerAsyncClient blobContainerAsyncClient(AzureStorageBlobProperties properties,
                                                      BlobServiceAsyncClient blobServiceAsyncClient) {
        return blobServiceAsyncClient.getBlobContainerAsyncClient(properties.getContainerName());
    }

    /**
     * Autoconfigure the {@link BlobServiceAsyncClient} instance.
     * @param builder the {@link BlobServiceClientBuilder} to build the instance.
     * @return the blob service async client.
     */
    @Bean
    @ConditionalOnMissingBean
    public BlobServiceAsyncClient blobServiceAsyncClient(
        @Qualifier(STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME) BlobServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageBlobProperties.PREFIX, name = "blob-name")
    BlobClient blobClient(AzureStorageBlobProperties properties,
                          BlobContainerClient blobContainerClient) {
        return blobContainerClient.getBlobClient(properties.getBlobName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageBlobProperties.PREFIX, name = "container-name")
    BlobContainerClient blobContainerClient(AzureStorageBlobProperties properties,
                                            BlobServiceClient blobServiceClient) {
        return blobServiceClient.getBlobContainerClient(properties.getContainerName());
    }

    /**
     * Autoconfigure the {@link BlobServiceClient} instance.
     * @param builder the {@link BlobServiceClientBuilder} to build the instance.
     * @return the blob service client.
     */
    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClient blobServiceClient(
        @Qualifier(STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME) BlobServiceClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean(STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    BlobServiceClientBuilderFactory blobServiceClientBuilderFactory(
        AzureStorageBlobProperties properties,
        ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.StorageBlob>> connectionStringProviders,
        ObjectProvider<AzureServiceClientBuilderCustomizer<BlobServiceClientBuilder>> customizers) {
        BlobServiceClientBuilderFactory factory = new BlobServiceClientBuilderFactory(properties);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_STORAGE_BLOB);
        connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    @Bean(STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME)
    @ConditionalOnMissingBean(name = STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME)
    BlobServiceClientBuilder blobServiceClientBuilder(@Qualifier(STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME)
                                                          BlobServiceClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnProperty("spring.cloud.azure.storage.blob.connection-string")
    StaticConnectionStringProvider<AzureServiceType.StorageBlob> staticStorageBlobConnectionStringProvider(
        AzureStorageBlobProperties properties) {
        return new StaticConnectionStringProvider<>(AzureServiceType.STORAGE_BLOB, properties.getConnectionString());
    }

}
