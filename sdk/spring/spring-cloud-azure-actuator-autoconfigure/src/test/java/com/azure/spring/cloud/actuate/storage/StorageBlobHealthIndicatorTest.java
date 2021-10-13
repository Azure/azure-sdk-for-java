// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.storage;

import com.azure.core.http.rest.Response;
import com.azure.spring.cloud.actuate.autoconfigure.storage.StorageBlobHealthConfiguration;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobServiceProperties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import static com.azure.spring.cloud.actuate.storage.StorageHealthConstants.URL_FIELD;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageBlobHealthIndicatorTest {

    private static final String MOCK_URL = "https://example.org/bigly_fake_url";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(StorageBlobHealthConfiguration.class));

    @Test
    void configureWithNoStorageBlobClient() {
        this.contextRunner.run(context -> Assertions.assertThat(context).doesNotHaveBean(StorageBlobHealthIndicator.class));
    }

    @Test
    void configureWithStorageBlobClientUp() {
        this.contextRunner
            .withUserConfiguration(TestConfigurationConnectionUp.class)
            .run(context -> {
                Assertions.assertThat(context).hasSingleBean(StorageBlobHealthIndicator.class);

                final StorageBlobHealthIndicator healthIndicator = context.getBean(StorageBlobHealthIndicator.class);
                Health health = healthIndicator.getHealth(true);

                assertEquals(Status.UP, health.getStatus());
                assertEquals(MOCK_URL, health.getDetails().get(URL_FIELD));
            });
    }

    @Test
    void configureWithStorageFileClientDown() {
        this.contextRunner
            .withUserConfiguration(TestConfigurationConnectionDown.class)
            .run(context -> {
                Assertions.assertThat(context).hasSingleBean(StorageBlobHealthIndicator.class);

                final StorageBlobHealthIndicator healthIndicator = context.getBean(StorageBlobHealthIndicator.class);
                Health health = healthIndicator.getHealth(true);

                assertEquals(Status.DOWN, health.getStatus());
                assertEquals(MOCK_URL, health.getDetails().get(URL_FIELD));
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfigurationConnectionUp {

        @Bean
        BlobServiceAsyncClient blobAsyncClient() {
            @SuppressWarnings("unchecked") Response<BlobServiceProperties> mockResponse =
                (Response<BlobServiceProperties>) Mockito.mock(
                    Response.class);

            BlobServiceAsyncClient mockAsyncClient = Mockito.mock(BlobServiceAsyncClient.class);
            Mockito.when(mockAsyncClient.getAccountUrl()).thenReturn(MOCK_URL);
            Mockito.when(mockAsyncClient.getPropertiesWithResponse()).thenReturn(Mono.just(mockResponse));
            return mockAsyncClient;
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfigurationConnectionDown {

        @Bean
        BlobServiceAsyncClient blobAsyncClient() {
            BlobServiceAsyncClient mockAsyncClient = Mockito.mock(BlobServiceAsyncClient.class);
            Mockito.when(mockAsyncClient.getAccountUrl()).thenReturn(MOCK_URL);
            Mockito.when(mockAsyncClient.getPropertiesWithResponse())
                   .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));
            return mockAsyncClient;
        }

    }
}
