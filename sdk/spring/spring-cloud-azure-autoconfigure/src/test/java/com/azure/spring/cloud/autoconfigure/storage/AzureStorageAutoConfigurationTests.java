// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.AzureStorageFileShareAutoConfiguration;
import com.azure.spring.cloud.service.implementation.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.storage.fileshare.ShareServiceClientBuilderFactory;
import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AzureStorageAutoConfigurationTests {

    private static final String STORAGE_CONNECTION_STRING_PATTERN = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageBlobAutoConfiguration.class))
        .withConfiguration(AutoConfigurations.of(AzureStorageFileShareAutoConfiguration.class));


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
}
