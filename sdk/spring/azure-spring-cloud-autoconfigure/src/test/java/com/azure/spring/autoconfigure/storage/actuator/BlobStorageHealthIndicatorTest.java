// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage.actuator;

import com.azure.spring.autoconfigure.storage.StorageAutoConfiguration;
import com.azure.spring.autoconfigure.storage.StorageHealthConfiguration;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.AccountKind;
import com.azure.storage.blob.models.SkuName;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.file.share.ShareServiceClientBuilder;
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

public class BlobStorageHealthIndicatorTest {

    private static final String MOCK_URL = "https://example.org/bigly_fake_url";

    @Test
    public void testWithNoStorageConfiguration() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withBean(BlobServiceClientBuilder.class)
            .withBean(ShareServiceClientBuilder.class)
            .withConfiguration(AutoConfigurations.of(StorageHealthConfiguration.class));

        contextRunner.run(context ->
            Assertions.assertThrows(IllegalStateException.class,
                () -> context.getBean(BlobStorageHealthIndicator.class).getHealth(true)));
    }

    @Test
    public void testWithStorageConfigurationWithConnectionUp() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class, StorageHealthConfiguration.class))
            .withUserConfiguration(TestConfigurationConnectionUp.class)
            .withPropertyValues("spring.cloud.azure.storage.account-name=acc1");

        contextRunner.run(context -> {
            Health health = context.getBean("blobStorageHealthIndicator", BlobStorageHealthIndicator.class)
                                   .getHealth(true);
            Assertions.assertEquals(Status.UP, health.getStatus());
            Assertions.assertEquals(MOCK_URL, health.getDetails().get(Constants.URL_FIELD));
        });
    }

    @Test
    public void testWithStorageConfigurationWithConnectionDown() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class, StorageHealthConfiguration.class))
            .withUserConfiguration(TestConfigurationConnectionDown.class)
            .withPropertyValues("spring.cloud.azure.storage.account-name=acc1");

        contextRunner.run(context -> {
            Health health = context.getBean("blobStorageHealthIndicator", BlobStorageHealthIndicator.class)
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

        @Bean
        ShareServiceClientBuilder shareServiceClientBuilder() {
            return mock(ShareServiceClientBuilder.class);
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

        @Bean
        ShareServiceClientBuilder shareServiceClientBuilder() {
            return mock(ShareServiceClientBuilder.class);
        }
    }
}
