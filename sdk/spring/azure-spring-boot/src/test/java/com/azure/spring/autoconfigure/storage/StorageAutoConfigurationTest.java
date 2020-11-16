// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage;

import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class StorageAutoConfigurationTest {

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class))
        .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzureStorageDisabled() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(new ClassPathResource("storage.enable.config")))
                          .run(context -> assertThat(context).doesNotHaveBean(StorageProperties.class));
    }

    @Test
    public void testWithoutStorageClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(BlobServiceClientBuilder.class))
                          .run(context -> assertThat(context).doesNotHaveBean(StorageProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureStoragePropertiesIllegal() {
        this.contextRunner.withPropertyValues("azure.storage.accountName=a")
                          .run(context -> context.getBean(StorageProperties.class));
    }

    @Test
    public void testAzureStoragePropertiesConfigured() {
        this.contextRunner.withPropertyValues("azure.storage.account-name=acc1")
                          .withPropertyValues("azure.storage.account-key=key1")
                          .withPropertyValues("azure.storage.blob-endpoint=endpoint1")
                          .run(context -> {
                              assertThat(context).hasSingleBean(StorageProperties.class);
                              final StorageProperties storageProperties = context.getBean(StorageProperties.class);
                              Assertions.assertThat(storageProperties.getAccountName()).isEqualTo("acc1");
                              Assertions.assertThat(storageProperties.getAccountKey()).isEqualTo("key1");
                              Assertions.assertThat(storageProperties.getBlobEndpoint()).isEqualTo("endpoint1");
                          });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        BlobServiceClientBuilder blobServiceClientBuilder() {
            return mock(BlobServiceClientBuilder.class);
        }

        @Bean
        ShareServiceClientBuilder shareServiceClientBuilder() {
            return mock(ShareServiceClientBuilder.class);
        }

    }
}
