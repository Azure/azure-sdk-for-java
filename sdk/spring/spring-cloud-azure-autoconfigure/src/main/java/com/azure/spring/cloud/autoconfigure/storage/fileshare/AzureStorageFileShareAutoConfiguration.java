// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.fileshare.properties.AzureStorageFileShareProperties;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.storage.fileshare.ShareServiceClientBuilderFactory;
import com.azure.storage.file.share.ShareAsyncClient;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryAsyncClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
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

    AzureStorageFileShareAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureStorageFileShareProperties.PREFIX)
    AzureStorageFileShareProperties azureStorageFileShareProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureStorageFileShareProperties());
    }

    /**
     * Autoconfigure the {@link ShareServiceClient} instance.
     * @param builder the {@link ShareServiceClientBuilder} to build the instance.
     * @return the share service client.
     */
    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClient shareServiceClient(ShareServiceClientBuilder builder) {
        return builder.buildClient();
    }

    /**
     * Autoconfigure the {@link ShareServiceAsyncClient} instance.
     * @param builder the {@link ShareServiceClientBuilder} to build the instance.
     * @return the share service async client.
     */
    @Bean
    @ConditionalOnMissingBean
    public ShareServiceAsyncClient shareServiceAsyncClient(ShareServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    ShareServiceClientBuilderFactory shareServiceClientBuilderFactory(
        AzureStorageFileShareProperties properties,
        ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.StorageFileShare>> connectionStringProviders,
        ObjectProvider<AzureServiceClientBuilderCustomizer<ShareServiceClientBuilder>> customizers) {
        ShareServiceClientBuilderFactory factory = new ShareServiceClientBuilderFactory(properties);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_STORAGE_FILES);
        connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    ShareServiceClientBuilder shareServiceClientBuilder(ShareServiceClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "share-name")
    ShareAsyncClient shareAsyncClient(AzureStorageFileShareProperties properties,
                                             ShareServiceAsyncClient shareServiceAsyncClient) {
        return shareServiceAsyncClient.getShareAsyncClient(properties.getShareName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "share-name")
    ShareClient shareClient(AzureStorageFileShareProperties properties, ShareServiceClient shareServiceClient) {
        return shareServiceClient.getShareClient(properties.getShareName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "file-path")
    ShareFileAsyncClient shareFileAsyncClient(AzureStorageFileShareProperties properties,
                                                     ShareAsyncClient shareAsyncClient) {
        return shareAsyncClient.getFileClient(properties.getFilePath());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "file-path")
    ShareFileClient shareFileClient(AzureStorageFileShareProperties properties,
                                           ShareClient shareClient) {
        return shareClient.getFileClient(properties.getFilePath());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "directory-path")
    ShareDirectoryAsyncClient shareDirectoryAsyncClient(AzureStorageFileShareProperties properties,
                                                               ShareAsyncClient shareAsyncClient) {
        return shareAsyncClient.getDirectoryClient(properties.getDirectoryPath());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "directory-path")
    ShareDirectoryClient shareDirectoryClient(AzureStorageFileShareProperties properties,
                                                     ShareClient shareClient) {
        return shareClient.getDirectoryClient(properties.getDirectoryPath());
    }

    @Bean
    @ConditionalOnProperty("spring.cloud.azure.storage.fileshare.connection-string")
    StaticConnectionStringProvider<AzureServiceType.StorageFileShare> staticStorageBlobConnectionStringProvider(
        AzureStorageFileShareProperties properties) {
        return new StaticConnectionStringProvider<>(AzureServiceType.STORAGE_FILE_SHARE, properties.getConnectionString());
    }


}
