// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage.actuator;

import com.azure.core.http.rest.Response;
import com.azure.spring.autoconfigure.storage.StorageAutoConfiguration;
import com.azure.spring.autoconfigure.storage.StorageHealthConfiguration;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.models.ShareServiceProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import static com.azure.spring.autoconfigure.storage.actuator.Constants.URL_FIELD;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileStorageHealthIndicatorTest {
    private static final String MOCK_URL = "https://example.org/bigly_fake_url";

    @Test
    public void testWithNoStorageConfiguration() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withBean(ShareServiceClientBuilder.class)
            .withConfiguration(AutoConfigurations.of(StorageHealthConfiguration.class));

        contextRunner.withBean(FileStorageHealthIndicator.class).run(context ->
            Assertions.assertThrows(IllegalStateException.class,
                () -> context.getBean(FileStorageHealthIndicator.class).getHealth(true)));
    }

    @Test
    public void testWithStorageConfigurationWithConnectionUp() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class, StorageHealthConfiguration.class))
            .withUserConfiguration(TestConfigurationConnectionUp.class)
            .withPropertyValues("spring.cloud.azure.storage.account-name=acc1");
        contextRunner.run(context -> {
            Health health = context.getBean(FileStorageHealthIndicator.class).getHealth(true);
            Assertions.assertEquals(Status.UP, health.getStatus());
            Assertions.assertEquals(MOCK_URL, health.getDetails().get(URL_FIELD));
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
            Health health = context.getBean(FileStorageHealthIndicator.class).getHealth(true);
            Assertions.assertEquals(Status.DOWN, health.getStatus());
            Assertions.assertEquals(MOCK_URL, health.getDetails().get(URL_FIELD));
        });
    }

    @Configuration
    static class TestConfigurationConnectionUp {

        @Bean
        ShareServiceClientBuilder shareServiceClientBuilder() {
            ShareServiceClientBuilder mockClientBuilder = mock(ShareServiceClientBuilder.class);
            ShareServiceAsyncClient mockAsyncClient = mock(ShareServiceAsyncClient.class);

            @SuppressWarnings("unchecked")
            Response<ShareServiceProperties> mockResponse = (Response<ShareServiceProperties>) mock(Response.class);

            when(mockAsyncClient.getFileServiceUrl()).thenReturn(MOCK_URL);
            when(mockAsyncClient.getPropertiesWithResponse()).thenReturn(Mono.just(mockResponse));
            when(mockClientBuilder.buildAsyncClient()).thenReturn(mockAsyncClient);

            return mockClientBuilder;
        }
    }

    @Configuration
    static class TestConfigurationConnectionDown {

        @Bean
        ShareServiceClientBuilder shareServiceClientBuilder() {
            ShareServiceClientBuilder mockClientBuilder = mock(ShareServiceClientBuilder.class);
            ShareServiceAsyncClient mockAsyncClient = mock(ShareServiceAsyncClient.class);
            when(mockAsyncClient.getFileServiceUrl()).thenReturn(MOCK_URL);
            when(mockAsyncClient.getPropertiesWithResponse())
                .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));
            when(mockClientBuilder.buildAsyncClient()).thenReturn(mockAsyncClient);

            return mockClientBuilder;
        }

    }
}
