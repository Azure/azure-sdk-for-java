// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.fileshare.AzureStorageFileShareAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.AzureStorageQueueAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.service.implementation.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.storage.fileshare.ShareServiceClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.storage.queue.QueueServiceClientBuilderFactory;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Used to test if Storage services work together.
 */
class AzureStorageAutoConfigurationTests {

    private static final String STORAGE_CONNECTION_STRING_PATTERN = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageBlobAutoConfiguration.class))
        .withConfiguration(AutoConfigurations.of(AzureStorageFileShareAutoConfiguration.class));

    @Test
    void configureWithStorageGlobalDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.enabled=false")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withUserConfiguration(AzureStorageQueueAutoConfiguration.class)
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureStorageBlobProperties.class);
                assertThat(context).doesNotHaveBean(AzureStorageFileShareAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureStorageQueueProperties.class);
            });
    }

    @Test
    void configureWithStorageGlobalEnabledAndServicesDisabled() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.enabled=true",
                "spring.cloud.azure.storage.blob.enabled=false",
                "spring.cloud.azure.storage.fileshare.enabled=false",
                "spring.cloud.azure.storage.queue.enabled=false"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withUserConfiguration(AzureStorageQueueAutoConfiguration.class)
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureStorageBlobProperties.class);
                assertThat(context).doesNotHaveBean(AzureStorageFileShareAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureStorageQueueProperties.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.blob.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void configureWithStorageGlobalAndBlobEnabled(String accountNameProperty) {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.enabled=true",
                "spring.cloud.azure.storage.blob.enabled=true",
                "spring.cloud.azure.storage.fileshare.enabled=false",
                "spring.cloud.azure.storage.queue.enabled=false",
                accountNameProperty
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withUserConfiguration(AzureStorageQueueAutoConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageBlobProperties.class);
                assertThat(context).doesNotHaveBean(AzureStorageFileShareAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureStorageQueueProperties.class);
            });
    }

    @Test
    void blobConfigShouldWorkWithFileShareConfig() {
        String accountName = "test-account-name";
        String connectionString = String.format(STORAGE_CONNECTION_STRING_PATTERN, accountName, "test-key");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.connection-string=" + connectionString,
                "spring.cloud.azure.storage.blob.account-name=test-account-name",
                "spring.cloud.azure.storage.fileshare.connection-string=" + connectionString,
                "spring.cloud.azure.storage.fileshare.account-name=test-account-name"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertNotNull(context.getBean("staticStorageBlobConnectionStringProvider"));
                assertNotNull(context.getBean("staticStorageFileShareConnectionStringProvider"));

                assertThat(context).hasSingleBean(BlobServiceClientBuilderFactory.class);
                assertThat(context).hasSingleBean(ShareServiceClientBuilderFactory.class);

                assertThat(context).hasSingleBean(BlobServiceClient.class);
            });
    }

    @Test
    void blobConfigShouldWorkWithFileShareConfigAndQueueConfig() {
        String accountName = "test-account-name";
        String connectionString = String.format(STORAGE_CONNECTION_STRING_PATTERN, accountName, "test-key");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.connection-string=" + connectionString,
                "spring.cloud.azure.storage.blob.account-name=test-account-name",
                "spring.cloud.azure.storage.fileshare.connection-string=" + connectionString,
                "spring.cloud.azure.storage.fileshare.account-name=test-account-name",
                "spring.cloud.azure.storage.queue.connection-string=" + connectionString,
                "spring.cloud.azure.storage.queue.account-name=test-account-name"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withUserConfiguration(AzureStorageQueueAutoConfiguration.class)
            .run(context -> {
                assertNotNull(context.getBean("staticStorageBlobConnectionStringProvider"));
                assertNotNull(context.getBean("staticStorageFileShareConnectionStringProvider"));
                assertNotNull(context.getBean("staticStorageQueueConnectionStringProvider"));

                assertThat(context).hasSingleBean(BlobServiceClientBuilderFactory.class);
                assertThat(context).hasSingleBean(ShareServiceClientBuilderFactory.class);
                assertThat(context).hasSingleBean(QueueServiceClientBuilderFactory.class);

                assertThat(context).hasSingleBean(BlobServiceClient.class);
                assertThat(context).hasSingleBean(ShareServiceClient.class);
                assertThat(context).hasSingleBean(QueueServiceClient.class);

                assertThat(context).hasSingleBean(BlobServiceAsyncClient.class);
                assertThat(context).hasSingleBean(ShareServiceAsyncClient.class);
                assertThat(context).hasSingleBean(QueueServiceAsyncClient.class);

                assertThat(context).hasSingleBean(BlobServiceClientBuilder.class);
                assertThat(context).hasSingleBean(ShareServiceClientBuilder.class);
                assertThat(context).hasSingleBean(QueueServiceClientBuilder.class);
            });
    }

    @Test
    void storageAllServiceShouldWorkWithGlobalConfig() {
        String accountName = "test-account-name";
        String connectionString = String.format(STORAGE_CONNECTION_STRING_PATTERN, accountName, "test-key");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.connection-string=" + connectionString,
                "spring.cloud.azure.storage.account-name=test-account-name"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withUserConfiguration(AzureStorageQueueAutoConfiguration.class)
            .run(context -> {
                assertNotNull(context.getBean("staticStorageBlobConnectionStringProvider"));
                assertNotNull(context.getBean("staticStorageFileShareConnectionStringProvider"));
                assertNotNull(context.getBean("staticStorageQueueConnectionStringProvider"));

                assertThat(context).hasSingleBean(BlobServiceClientBuilderFactory.class);
                assertThat(context).hasSingleBean(ShareServiceClientBuilderFactory.class);
                assertThat(context).hasSingleBean(QueueServiceClientBuilderFactory.class);

                assertThat(context).hasSingleBean(BlobServiceClient.class);
                assertThat(context).hasSingleBean(ShareServiceClient.class);
                assertThat(context).hasSingleBean(QueueServiceClient.class);

                assertThat(context).hasSingleBean(BlobServiceAsyncClient.class);
                assertThat(context).hasSingleBean(ShareServiceAsyncClient.class);
                assertThat(context).hasSingleBean(QueueServiceAsyncClient.class);

                assertThat(context).hasSingleBean(BlobServiceClientBuilder.class);
                assertThat(context).hasSingleBean(ShareServiceClientBuilder.class);
                assertThat(context).hasSingleBean(QueueServiceClientBuilder.class);
            });
    }

}
