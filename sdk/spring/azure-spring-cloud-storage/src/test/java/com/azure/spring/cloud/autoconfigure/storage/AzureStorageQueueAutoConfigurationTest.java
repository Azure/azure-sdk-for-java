// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.integration.storage.queue.factory.DefaultStorageQueueClientFactory;
import com.azure.storage.queue.QueueServiceClient;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureStorageQueueAutoConfigurationTest {

    private ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureStorageQueueAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzureStorageDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.queue.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageProperties.class));
    }

    @Test
    public void testWithoutCloudQueueClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(QueueServiceClient.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureStoragePropertiesIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=a")
            .run(context -> context.getBean(AzureStorageProperties.class));
    }

    @Test
    public void testAzureStoragePropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=squeue").run(context -> {
            assertThat(context).hasSingleBean(AzureStorageProperties.class);
            assertThat(context.getBean(AzureStorageProperties.class).getAccount()).isEqualTo("squeue");
        });
    }

    @Configuration
    static class TestConfiguration {
        @Bean
        DefaultStorageQueueClientFactory defaultStorageQueueClientFactory() {
            return mock(DefaultStorageQueueClientFactory.class);
        }
    }
}
