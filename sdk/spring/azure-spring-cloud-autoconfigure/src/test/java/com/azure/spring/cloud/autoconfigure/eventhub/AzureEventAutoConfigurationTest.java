// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.StaticConnectionStringProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureEventAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubAutoConfiguration.class));

    @Test
    void configureWithoutEventHubClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubClientBuilder.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    void configureWithEventHubDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhub.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    void configureWithoutConnectionStringAndNamespace() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(AzureCosmosProperties.class));
    }

    @Test
    void configureWithNamespace() {
        final EventHubClientBuilder mockEventHubClientBuilder = mockEventHubClientBuilder();

        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhub.namespace=test-eventhub-namespace")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(EventHubClientBuilder.class, () -> mockEventHubClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubProperties.class);
                assertThat(context).doesNotHaveBean(StaticConnectionStringProvider.class);
            });
    }

    @Test
    void configureWithConnectionString() {
        final EventHubClientBuilder mockEventHubClientBuilder = mockEventHubClientBuilder();

        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhub.connection-string=test-connection-string")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(EventHubClientBuilder.class, () -> mockEventHubClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubProperties.class);
                assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
            });
    }

    private EventHubClientBuilder mockEventHubClientBuilder() {
        final EventHubClientBuilder mockEventHubClientBuilder = mock(EventHubClientBuilder.class);
        when(mockEventHubClientBuilder.buildProducerClient()).thenReturn(mock(EventHubProducerClient.class));
        when(mockEventHubClientBuilder.buildAsyncProducerClient()).thenReturn(mock(EventHubProducerAsyncClient.class));
        when(mockEventHubClientBuilder.buildConsumerClient()).thenReturn(mock(EventHubConsumerClient.class));
        when(mockEventHubClientBuilder.buildAsyncConsumerClient()).thenReturn(mock(EventHubConsumerAsyncClient.class));
        return mockEventHubClientBuilder;
    }

}
