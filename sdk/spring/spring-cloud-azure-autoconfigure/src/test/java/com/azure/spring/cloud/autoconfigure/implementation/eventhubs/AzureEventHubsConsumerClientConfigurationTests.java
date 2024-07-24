// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.spring.cloud.autoconfigure.implementation.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.implementation.eventhubs.EventHubsTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubsConsumerClientConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsConsumerClientConfiguration.class));

    @Test
    void noEventHubNameProvidedShouldNotConfigure() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsConsumerClientConfiguration.class));
    }

    @Test
    void eventHubNameAndConsumerGroupProvidedShouldConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub",
                "spring.cloud.azure.eventhubs.consumer.consumer-group=test-consumer-group"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsConsumerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubsConsumerClientConfiguration.SharedConsumerConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubsConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class);
            });

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.consumer.event-hub-name=test-eventhub",
                "spring.cloud.azure.eventhubs.consumer.consumer-group=test-consumer-group"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsConsumerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubsConsumerClientConfiguration.SharedConsumerConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubsConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class);
            });
    }

    @Test
    void withGlobalEventHubConnectionSetShouldConfigureShared() {
        String namespace = "test-namespace";
        String eventHubName = "test-eventhub";
        String consumerGroupName = "test-consumer-group";

        EventHubClientBuilder clientBuilder = new EventHubClientBuilder()
            .consumerGroup(consumerGroupName)
            .connectionString(String.format(CONNECTION_STRING_FORMAT, namespace), eventHubName);

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=" + namespace,
                "spring.cloud.azure.eventhubs.event-hub-name=" + eventHubName,
                "spring.cloud.azure.eventhubs.consumer.consumer-group=" + consumerGroupName
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(EventHubClientBuilder.class, () -> clientBuilder)
            .run(
                context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubsConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubsConsumerClientConfiguration.SharedConsumerConnectionConfiguration.class);
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
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withUserConfiguration(AzureEventHubsAutoConfiguration.class)
            .run(
                context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubsConsumerClientConfiguration.SharedConsumerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubsConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(EventHubConsumerAsyncClient.class);
                    assertThat(context).hasSingleBean(EventHubConsumerClient.class);
                    assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME);
                    assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME);
                }
            );
    }

    @Test
    void customizerShouldBeCalled() {
        EventHubBuilderCustomizer customizer = new EventHubBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.consumer.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.consumer.event-hub-name=test-event-hub",
                "spring.cloud.azure.eventhubs.consumer.consumer-group=test-consumer-group",
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean("customizer1", EventHubBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", EventHubBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        EventHubBuilderCustomizer customizer = new EventHubBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.consumer.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.consumer.event-hub-name=test-event-hub",
                "spring.cloud.azure.eventhubs.consumer.consumer-group=test-consumer-group",
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean("customizer1", EventHubBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", EventHubBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    private static class EventHubBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

}
