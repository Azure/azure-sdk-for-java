// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.cloud.autoconfigure.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.properties.AzureStorageFileShareProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.connectionstring.ConnectionStringProvider;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.implementation.storage.fileshare.ShareServiceClientBuilderFactory;
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

    public AzureStorageFileShareAutoConfiguration(AzureGlobalProperties azureGlobalProperties) {
        super(azureGlobalProperties);
    }

    @Bean
    @ConfigurationProperties(AzureStorageFileShareProperties.PREFIX)
    public AzureStorageFileShareProperties azureStorageFileShareProperties() {
        return loadProperties(this.azureGlobalProperties, new AzureStorageFileShareProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClient shareServiceClient(ShareServiceClientBuilder builder) {
        return builder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceAsyncClient shareServiceAsyncClient(ShareServiceClientBuilder builder) {
        return builder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    ShareServiceClientBuilderFactory shareServiceClientBuilderFactory(
        AzureStorageFileShareProperties properties,
        ObjectProvider<ConnectionStringProvider<AzureServiceType.StorageFileShare>> connectionStringProviders,
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
    public ShareAsyncClient shareAsyncClient(AzureStorageFileShareProperties properties,
                                             ShareServiceAsyncClient shareServiceAsyncClient) {
        return shareServiceAsyncClient.getShareAsyncClient(properties.getShareName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "share-name")
    public ShareClient shareClient(AzureStorageFileShareProperties properties, ShareServiceClient shareServiceClient) {
        return shareServiceClient.getShareClient(properties.getShareName());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "file-path")
    public ShareFileAsyncClient shareFileAsyncClient(AzureStorageFileShareProperties properties,
                                                     ShareAsyncClient shareAsyncClient) {
        return shareAsyncClient.getFileClient(properties.getFilePath());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "file-path")
    public ShareFileClient shareFileClient(AzureStorageFileShareProperties properties,
                                           ShareClient shareClient) {
        return shareClient.getFileClient(properties.getFilePath());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "directory-path")
    public ShareDirectoryAsyncClient shareDirectoryAsyncClient(AzureStorageFileShareProperties properties,
                                                               ShareAsyncClient shareAsyncClient) {
        return shareAsyncClient.getDirectoryClient(properties.getDirectoryPath());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageFileShareProperties.PREFIX, name = "directory-path")
    public ShareDirectoryClient shareDirectoryClient(AzureStorageFileShareProperties properties,
                                                     ShareClient shareClient) {
        return shareClient.getDirectoryClient(properties.getDirectoryPath());
    }

    @Bean
    @ConditionalOnProperty("spring.cloud.azure.storage.fileshare.connection-string")
    public StaticConnectionStringProvider<AzureServiceType.StorageFileShare> staticStorageBlobConnectionStringProvider(
        AzureStorageFileShareProperties properties) {
        return new StaticConnectionStringProvider<>(AzureServiceType.STORAGE_FILE_SHARE, properties.getConnectionString());
    }


}
