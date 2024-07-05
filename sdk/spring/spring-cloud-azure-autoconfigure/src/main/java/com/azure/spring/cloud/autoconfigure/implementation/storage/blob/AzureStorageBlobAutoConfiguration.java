// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.blob;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureServicePropertiesUtils;
import com.azure.spring.cloud.autoconfigure.implementation.storage.AzureStorageConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Storage Blob support.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass(BlobServiceClientBuilder.class)
@ConditionalOnProperty(value = { "spring.cloud.azure.storage.blob.enabled",  "spring.cloud.azure.storage.enabled" }, havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(
    prefixes = { "spring.cloud.azure.storage.blob", "spring.cloud.azure.storage" },
    name = { "account-name", "endpoint", "connection-string" })
@Import(AzureStorageConfiguration.class)
public class AzureStorageBlobAutoConfiguration {

    @Bean
    @ConfigurationProperties(AzureStorageBlobProperties.PREFIX)
    AzureStorageBlobProperties azureStorageBlobProperties(@Qualifier("azureStorageProperties") AzureStorageProperties azureStorageProperties) {
        return AzureServicePropertiesUtils.loadServiceCommonProperties(azureStorageProperties, new AzureStorageBlobProperties());
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

    @Bean
    @ConditionalOnMissingBean
    BlobServiceAsyncClient blobServiceAsyncClient(
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

    @Bean
    @ConditionalOnMissingBean
    BlobServiceClient blobServiceClient(
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
    @ConditionalOnAnyProperty(prefixes = { AzureStorageBlobProperties.PREFIX, AzureStorageProperties.PREFIX }, name = { "connection-string" })
    StaticConnectionStringProvider<AzureServiceType.StorageBlob> staticStorageBlobConnectionStringProvider(
        AzureStorageBlobProperties properties) {
        return new StaticConnectionStringProvider<>(AzureServiceType.STORAGE_BLOB, properties.getConnectionString());
    }

}
