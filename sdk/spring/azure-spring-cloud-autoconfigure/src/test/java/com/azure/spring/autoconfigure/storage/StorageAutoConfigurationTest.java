// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage;

import com.azure.spring.autoconfigure.unity.AzurePropertyAutoConfiguration;
import com.azure.spring.autoconfigure.unity.AzureProperties;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import static com.azure.spring.autoconfigure.unity.AzureProperties.AZURE_PROPERTY_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class StorageAutoConfigurationTest {

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class, AzurePropertyAutoConfiguration.class))
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

    @Test
    public void testAzureStoragePropertiesIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.accountName=a")
                          .run(context -> assertThrows(IllegalStateException.class,
                              () -> context.getBean(StorageProperties.class)));
    }

    @Test
    public void testAzureStoragePropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account-name=acc1")
                          .withPropertyValues("spring.cloud.azure.storage.account-key=key1")
                          .withPropertyValues("spring.cloud.azure.storage.blob-endpoint=endpoint1")
                          .withPropertyValues("spring.cloud.azure.storage.credential.client-id=for-test-purpose")
                          .withPropertyValues("spring.cloud.azure.storage.environment.cloud=AzureUSGovernment")
                          .run(context -> {
                              assertThat(context).hasSingleBean(StorageProperties.class);
                              final StorageProperties storageProperties = context.getBean(StorageProperties.class);
                              assertThat(storageProperties.getAccountName()).isEqualTo("acc1");
                              assertThat(storageProperties.getAccountKey()).isEqualTo("key1");
                              assertThat(storageProperties.getBlobEndpoint()).isEqualTo("endpoint1");
                              assertThat(storageProperties.getCredential().getClientId()).isEqualTo("for-test-purpose");
                              assertThat(storageProperties.getEnvironment().getCloud()).isEqualTo("AzureUSGovernment");
                          });
    }

    @Test
    public void testAzurePropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.credential.client-id=for-test-purpose")
                          .withPropertyValues("spring.cloud.azure.environment.cloud=AzureUSGovernment")
                          .withPropertyValues("spring.cloud.azure.storage.account-name=acc1")
                          .run(context -> {
                              assertThat(context).hasSingleBean(StorageProperties.class);
                              assertThat(context).hasBean(AZURE_PROPERTY_BEAN_NAME);

                              final AzureProperties azureProperties = (AzureProperties) context.getBean(AZURE_PROPERTY_BEAN_NAME);
                              assertThat(azureProperties.getCredential().getClientId()).isEqualTo("for-test-purpose");
                              assertThat(azureProperties.getEnvironment().getCloud()).isEqualTo("AzureUSGovernment");
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
