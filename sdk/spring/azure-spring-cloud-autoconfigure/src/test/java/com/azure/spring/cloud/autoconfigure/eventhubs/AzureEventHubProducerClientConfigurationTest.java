// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.spring.cloud.autoconfigure.context.AzureContextUtils;
import com.azure.spring.cloud.autoconfigure.eventhubs.factory.EventHubClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.eventhubs.EventHubsTestUtils.CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubProducerClientConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubProducerClientConfiguration.class));

    @Test
    void noEventHubNameProvidedShouldNotConfigure() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProducerClientConfiguration.class));
    }

    @Test
    void eventHubNameProvidedShouldConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubProducerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
            });

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.producer.event-hub-name=test-eventhub"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubProducerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
            });
    }

    @Test
    void withGlobalEventHubConnectionSetShouldConfigureShared() {
        String namespace = "test-namespace";
        String eventHubName = "test-eventhub";
        String consumerGroupName = "test-consumer-group";

        EventHubClientBuilder clientBuilder = new EventHubClientBuilder()
            .consumerGroup(consumerGroupName)
            .connectionString(String.format(CONNECTION_STRING, namespace), eventHubName);

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=" + namespace,
                "spring.cloud.azure.eventhubs.event-hub-name=" + eventHubName
            )
            .withBean(EventHubClientBuilder.class, () -> clientBuilder)
            .run(
                context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(EventHubProducerClient.class);
                    assertThat(context).hasSingleBean(EventHubProducerAsyncClient.class);
                }
            );
    }

    @Test
    void withDedicatedEvenHubConnectionSetShouldConfigureDedicated() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.producer.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.producer.event-hub-name=test-event-hub",
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
                )
            .withUserConfiguration(AzureEventHubPropertiesTestConfiguration.class)
            .withBean(EventHubClientBuilder.class, EventHubClientBuilder::new)
            .run(
                context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(EventHubClientBuilderFactory.class);
                    assertThat(context).hasSingleBean(EventHubProducerClient.class);
                    assertThat(context).hasSingleBean(EventHubProducerAsyncClient.class);
                    assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME);
                    assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME);
                }
            );
    }

}
