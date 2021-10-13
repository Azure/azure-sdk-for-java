// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.spring.cloud.autoconfigure.context.AzureContextUtils;
import com.azure.spring.cloud.autoconfigure.eventhubs.factory.EventHubClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.eventhubs.EventHubsTestUtils.CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubConsumerClientConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubConsumerClientConfiguration.class));

    @Test
    void noEventHubNameProvidedShouldNotConfigure() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureEventHubConsumerClientConfiguration.class));
    }

    @Test
    void eventHubNameAndConsumerGroupProvidedShouldConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub",
                "spring.cloud.azure.eventhubs.consumer.consumer-group=test-consumer-group"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubConsumerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubConsumerClientConfiguration.SharedConsumerConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class);
            });

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.consumer.event-hub-name=test-eventhub",
                "spring.cloud.azure.eventhubs.consumer.consumer-group=test-consumer-group"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubConsumerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubConsumerClientConfiguration.SharedConsumerConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class);
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
                "spring.cloud.azure.eventhubs.event-hub-name=" + eventHubName,
                "spring.cloud.azure.eventhubs.consumer.consumer-group=" + consumerGroupName
            )
            .withBean(EventHubClientBuilder.class, () -> clientBuilder)
            .run(
                context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubConsumerClientConfiguration.SharedConsumerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(EventHubConsumerAsyncClient.class);
                    assertThat(context).hasSingleBean(EventHubConsumerClient.class);
                }
            );
    }

    @Test
    void withDedicatedEvenHubConnectionSetShouldConfigureDedicated() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.consumer.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.consumer.event-hub-name=test-event-hub",
                "spring.cloud.azure.eventhubs.consumer.consumer-group=test-consumer-group",
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .withUserConfiguration(AzureEventHubPropertiesTestConfiguration.class)
            .withBean(EventHubClientBuilder.class, EventHubClientBuilder::new)
            .run(
                context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubConsumerClientConfiguration.SharedConsumerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(EventHubClientBuilderFactory.class);
                    assertThat(context).hasSingleBean(EventHubConsumerAsyncClient.class);
                    assertThat(context).hasSingleBean(EventHubConsumerClient.class);
                    assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME);
                    assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME);
                }
            );
    }

}
