// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.storage;

import com.azure.core.http.rest.Response;
import com.azure.spring.cloud.actuate.autoconfigure.storage.StorageFileHealthConfiguration;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.models.ShareServiceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import static com.azure.spring.cloud.actuate.storage.Constants.URL_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageFileHealthIndicatorTest {

    private static final String MOCK_URL = "https://example.org/bigly_fake_url";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(StorageFileHealthConfiguration.class));

    @Test
    void configureWithNoStorageFileClient() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(StorageFileHealthIndicator.class));
    }

    @Test
    void configureWithStorageFileClientUp() {
        this.contextRunner
            .withUserConfiguration(TestConfigurationConnectionUp.class)
            .run(context -> {
                assertThat(context).hasSingleBean(StorageFileHealthIndicator.class);

                final StorageFileHealthIndicator healthIndicator = context.getBean(StorageFileHealthIndicator.class);
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
                assertThat(context).hasSingleBean(StorageFileHealthIndicator.class);

                final StorageFileHealthIndicator healthIndicator = context.getBean(StorageFileHealthIndicator.class);
                Health health = healthIndicator.getHealth(true);

                assertEquals(Status.DOWN, health.getStatus());
                assertEquals(MOCK_URL, health.getDetails().get(URL_FIELD));
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfigurationConnectionUp {

        @Bean
        ShareServiceAsyncClient shareServiceAsyncClient() {
            ShareServiceAsyncClient mockAsyncClient = mock(ShareServiceAsyncClient.class);

            @SuppressWarnings("unchecked") Response<ShareServiceProperties> mockResponse =
                (Response<ShareServiceProperties>) mock(
                Response.class);

            when(mockAsyncClient.getFileServiceUrl()).thenReturn(MOCK_URL);
            when(mockAsyncClient.getPropertiesWithResponse()).thenReturn(Mono.just(mockResponse));

            return mockAsyncClient;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfigurationConnectionDown {

        @Bean
        ShareServiceAsyncClient shareServiceAsyncClient() {
            ShareServiceAsyncClient mockAsyncClient = mock(ShareServiceAsyncClient.class);
            when(mockAsyncClient.getFileServiceUrl()).thenReturn(MOCK_URL);
            when(mockAsyncClient.getPropertiesWithResponse()).thenReturn(
                Mono.error(new IllegalStateException("The gremlins have cut the cable.")));

            return mockAsyncClient;
        }

    }
}
