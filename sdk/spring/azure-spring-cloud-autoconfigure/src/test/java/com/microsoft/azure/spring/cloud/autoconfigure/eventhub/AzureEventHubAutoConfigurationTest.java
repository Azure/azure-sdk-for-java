// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import com.microsoft.azure.spring.integration.eventhub.factory.EventHubConnectionStringProvider;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AzureEventHubAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubAutoConfiguration.class))
        .withUserConfiguration(TestConfiguration.class);

    @Test
    public void testAzureEventHubDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    public void testWithoutEventHubClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(EventHubConsumerAsyncClient.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureEventHubPropertiesNamespaceIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=")
            .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=")
            .run(context -> context.getBean(AzureEventHubProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureEventHubPropertiesStorageAccountIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=nsl")
            .withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=1")
            .run(context -> context.getBean(AzureEventHubProperties.class));
    }

    @Test
    public void testAzureEventHubPropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.namespace=ns1").
            withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=sa1").run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubProperties.class);
                assertThat(context.getBean(AzureEventHubProperties.class).getNamespace()).isEqualTo("ns1");
                assertThat(context.getBean(AzureEventHubProperties.class).getCheckpointStorageAccount()).isEqualTo("sa1");
                assertThat(context).hasSingleBean(EventHubClientFactory.class);
                assertThat(context).hasSingleBean(EventHubOperation.class);
            });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        EventHubClientFactory clientFactory() {
            return mock(EventHubClientFactory.class);
        }

        @Bean
        EventHubConnectionStringProvider connectionStringProvider() {
            return mock(EventHubConnectionStringProvider.class);
        }
    }
}
