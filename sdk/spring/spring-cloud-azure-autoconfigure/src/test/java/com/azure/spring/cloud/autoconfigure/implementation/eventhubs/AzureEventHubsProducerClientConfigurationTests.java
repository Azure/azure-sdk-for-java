// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.spring.cloud.autoconfigure.implementation.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.implementation.eventhubs.EventHubsTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubsProducerClientConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsProducerClientConfiguration.class));

    @Test
    void noEventHubNameProvidedShouldNotConfigure() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsProducerClientConfiguration.class));
    }

    @Test
    void eventHubNameProvidedShouldConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.event-hub-name=test-eventhub"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsProducerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubsProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubsProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
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
                "spring.cloud.azure.eventhubs.event-hub-name=" + eventHubName
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(AzureContextUtils.EVENT_HUB_CLIENT_BUILDER_BEAN_NAME, EventHubClientBuilder.class, () -> clientBuilder)
            .run(
                context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubsProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubsProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(EventHubProducerClient.class);
                    assertThat(context).hasSingleBean(EventHubProducerAsyncClient.class);
                }
            );
    }

    @Test
    void sharedProducerInjectsRootBuilderWhenConsumerHasDedicatedOverride() {
        // Regression for issue #49245: when both a global event-hub-name and a consumer-only override exist,
        // the shared producer should still bind to the root builder, not the consumer's dedicated builder.
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=base-eventhub",
                "spring.cloud.azure.eventhubs.consumer.event-hub-name=override-eventhub",
                "spring.cloud.azure.eventhubs.consumer.consumer-group=test-consumer-group"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withUserConfiguration(AzureEventHubsAutoConfiguration.class)
            .run(
                context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                    assertThat(context).doesNotHaveBean(AzureEventHubsProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
                    // Consumer dedicated must be active so multiple EventHubClientBuilder beans coexist,
                    // proving the shared producer is selecting the root builder by qualifier rather than
                    // succeeding by accident because only one builder bean exists.
                    assertThat(context).hasSingleBean(AzureEventHubsConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class);
                    assertThat(context.getBeansOfType(EventHubClientBuilder.class)).hasSizeGreaterThan(1);
                    assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_CLIENT_BUILDER_BEAN_NAME);
                    assertThat(context).hasSingleBean(EventHubProducerClient.class);
                    // Pin the shared producer to the root builder: it must target base-eventhub,
                    // never the consumer-dedicated override-eventhub. This is the actual #49245 invariant.
                    assertThat(context.getBean(EventHubProducerClient.class).getEventHubName()).isEqualTo("base-eventhub");
                    assertThat(context.getBean(EventHubProducerAsyncClient.class).getEventHubName()).isEqualTo("base-eventhub");
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
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withUserConfiguration(AzureEventHubsAutoConfiguration.class)
            .run(
                context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubsProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubsProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(EventHubProducerClient.class);
                    assertThat(context).hasSingleBean(EventHubProducerAsyncClient.class);
                    assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME);
                    assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME);
                }
            );
    }

    @Test
    void producerEventHubNameOverrideShouldConfigureDedicated() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.event-hub-name=base-eventhub",
                "spring.cloud.azure.eventhubs.producer.event-hub-name=override-eventhub"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withUserConfiguration(AzureEventHubsAutoConfiguration.class)
            .run(
                context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubsProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubsProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
                    assertThat(context).hasSingleBean(EventHubProducerAsyncClient.class);
                    assertThat(context).hasSingleBean(EventHubProducerClient.class);

                    AzureEventHubsProperties properties = context.getBean(AzureEventHubsProperties.class);
                    AzureEventHubsProperties.Producer producer = properties.buildProducerProperties();
                    assertThat(producer.getEventHubName()).isEqualTo("override-eventhub");
                    assertThat(producer.getNamespace()).isEqualTo("test-namespace");
                }
            );
    }

    @Test
    void customizerShouldBeCalled() {
        EventHubBuilderCustomizer customizer = new EventHubBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.producer.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.producer.event-hub-name=test-event-hub",
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
                "spring.cloud.azure.eventhubs.producer.namespace=test-namespace",
                "spring.cloud.azure.eventhubs.producer.event-hub-name=test-event-hub",
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
