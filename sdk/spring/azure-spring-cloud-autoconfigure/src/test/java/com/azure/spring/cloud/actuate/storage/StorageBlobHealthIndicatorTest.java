// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.storage;

import com.azure.spring.cloud.actuate.autoconfigure.storage.StorageBlobHealthConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobClientAutoConfiguration;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.AccountKind;
import com.azure.storage.blob.models.SkuName;
import com.azure.storage.blob.models.StorageAccountInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StorageBlobHealthIndicatorTest {

    private static final String MOCK_URL = "https://example.org/bigly_fake_url";

    @Test
    public void testWithNoStorageConfiguration() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withBean(BlobServiceClientBuilder.class)
            .withConfiguration(AutoConfigurations.of(StorageBlobHealthConfiguration.class));

        contextRunner.run(context ->
                              Assertions.assertThrows(IllegalStateException.class,
                                                      () -> context.getBean(StorageBlobHealthIndicator.class)
                                                                   .getHealth(true)));
    }

    @Test
    public void testWithStorageConfigurationWithConnectionUp() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withConfiguration(AutoConfigurations.of(AzureStorageBlobClientAutoConfiguration.class, StorageBlobHealthConfiguration.class))
            .withUserConfiguration(TestConfigurationConnectionUp.class);

        contextRunner.run(context -> {
            Health health = context.getBean("blobStorageHealthIndicator", StorageBlobHealthIndicator.class)
                                   .getHealth(true);
            Assertions.assertEquals(Status.UP, health.getStatus());
            Assertions.assertEquals(MOCK_URL, health.getDetails().get(Constants.URL_FIELD));
        });
    }

    @Test
    public void testWithStorageConfigurationWithConnectionDown() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withConfiguration(AutoConfigurations.of(AzureStorageBlobClientAutoConfiguration.class, StorageBlobHealthConfiguration.class))
            .withUserConfiguration(TestConfigurationConnectionDown.class);

        contextRunner.run(context -> {
            Health health = context.getBean("blobStorageHealthIndicator", StorageBlobHealthIndicator.class)
                                   .getHealth(true);
            Assertions.assertEquals(Status.DOWN, health.getStatus());
            Assertions.assertEquals(MOCK_URL, health.getDetails().get(Constants.URL_FIELD));
        });
    }

    @Configuration
    static class TestConfigurationConnectionUp {

        @Bean
        BlobServiceClientBuilder blobServiceClientBuilder() {
            BlobServiceClientBuilder mockClientBuilder = mock(BlobServiceClientBuilder.class);
            BlobServiceAsyncClient mockAsyncClient = mock(BlobServiceAsyncClient.class);
            when(mockAsyncClient.getAccountUrl()).thenReturn(MOCK_URL);
            when(mockAsyncClient.getAccountInfo()).thenReturn(Mono.just(new StorageAccountInfo(SkuName.STANDARD_LRS,
                AccountKind.BLOB_STORAGE)));
            when(mockClientBuilder.buildAsyncClient()).thenReturn(mockAsyncClient);

            return mockClientBuilder;
        }

    }

    @Configuration
    static class TestConfigurationConnectionDown {

        @Bean
        BlobServiceClientBuilder blobServiceClientBuilder() {
            BlobServiceClientBuilder mockClientBuilder = mock(BlobServiceClientBuilder.class);
            BlobServiceAsyncClient mockAsyncClient = mock(BlobServiceAsyncClient.class);
            when(mockAsyncClient.getAccountUrl()).thenReturn(MOCK_URL);
            when(mockAsyncClient.getAccountInfo())
                .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));
            when(mockClientBuilder.buildAsyncClient()).thenReturn(mockAsyncClient);

            return mockClientBuilder;
        }

    }
}
