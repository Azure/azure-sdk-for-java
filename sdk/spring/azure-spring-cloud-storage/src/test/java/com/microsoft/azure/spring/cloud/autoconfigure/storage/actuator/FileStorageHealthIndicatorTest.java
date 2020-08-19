// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.http.HttpException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.models.ShareServiceProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.storage.AzureStorageAutoConfiguration;

import reactor.core.publisher.Mono;

public class FileStorageHealthIndicatorTest {
    private static final String MOCK_URL = "https://example.org/bigly_fake_url";

    @Test
    public void testWithNoStorageConfiguration() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withConfiguration(AutoConfigurations.of(AzureStorageActuatorAutoConfiguration.class));

        contextRunner.withBean(FileStorageHealthIndicator.class).run(context -> {
            Health health = context.getBean(FileStorageHealthIndicator.class).getHealth(true);
            Assert.assertEquals(AzureStorageActuatorConstants.NOT_CONFIGURED_STATUS, health.getStatus());
            Assert.assertEquals("No storage health details expected when storage not configured.", 0,
                    health.getDetails().size());
        });
    }

    @Test
    public void testWithStorageConfigurationAndNoAccount() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withConfiguration(AutoConfigurations.of(AzureStorageAutoConfiguration.class,
                AzureStorageActuatorAutoConfiguration.class))
            .withBean(FileStorageHealthIndicator.class);
        contextRunner.run(context -> {
            Health health = context.getBean(FileStorageHealthIndicator.class).getHealth(true);
            Assert.assertEquals(AzureStorageActuatorConstants.NOT_CONFIGURED_STATUS, health.getStatus());
        });
    }

    @Test
    public void testWithStorageConfigurationWithConnectionUp() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withConfiguration(AutoConfigurations.of(AzureEnvironmentAutoConfiguration.class,
                AzureStorageAutoConfiguration.class, AzureStorageActuatorAutoConfiguration.class))
            .withUserConfiguration(TestConfigurationConnectionUp.class)
            .withPropertyValues("spring.cloud.azure.storage.account=acc1")
            .withBean(FileStorageHealthIndicator.class);
        contextRunner.run(context -> {
            Health health = context.getBean(FileStorageHealthIndicator.class).getHealth(true);
            Assert.assertEquals(Status.UP, health.getStatus());
            Assert.assertEquals(MOCK_URL, health.getDetails().get(AzureStorageActuatorConstants.URL_FIELD));
        });
    }

    @Test
    public void testWithStorageConfigurationWithConnectionDown() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withAllowBeanDefinitionOverriding(true)
            .withConfiguration(AutoConfigurations.of(AzureEnvironmentAutoConfiguration.class,
                AzureStorageAutoConfiguration.class, AzureStorageActuatorAutoConfiguration.class))
            .withUserConfiguration(TestConfigurationConnectionDown.class)
            .withPropertyValues("spring.cloud.azure.storage.account=acc1")
            .withBean(BlobStorageHealthIndicator.class);
        contextRunner.run(context -> {
            Health health = context.getBean(FileStorageHealthIndicator.class).getHealth(true);
            Assert.assertEquals(Status.DOWN, health.getStatus());
            Assert.assertEquals(MOCK_URL, health.getDetails().get(AzureStorageActuatorConstants.URL_FIELD));
        });
    }

    @Configuration
    static class TestConfigurationConnectionUp {

        @Bean
        ShareServiceClientBuilder shareServiceClientBuilder() {
            ShareServiceClientBuilder mockClientBuilder = mock(ShareServiceClientBuilder.class);
            ShareServiceAsyncClient mockAsyncClient = mock(ShareServiceAsyncClient.class);

            @SuppressWarnings("unchecked")
            Response<ShareServiceProperties> mockResponse = (Response<ShareServiceProperties>) Mockito
                    .mock(Response.class);

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
                    .thenReturn(Mono.error(new HttpException("The gremlins have cut the cable.")));
            when(mockClientBuilder.buildAsyncClient()).thenReturn(mockAsyncClient);

            return mockClientBuilder;
        }

    }
}
