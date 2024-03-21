// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.implementation.config;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsProducerProperties;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointMode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtendedBindingHandlerMappingsProviderAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(EventHubsBinderConfiguration.class))
        .withUserConfiguration(TestApp.class)
        .withPropertyValues(
            "spring.cloud.stream.eventhubs.default.consumer.checkpoint.mode=batch",
            "spring.cloud.stream.eventhubs.default.consumer.load-balancing.strategy=balanced",
            "spring.cloud.stream.eventhubs.default.consumer.batch.max-size=6",
            "spring.cloud.stream.eventhubs.default.producer.sync=true",
            "spring.cloud.stream.eventhubs.default.producer.send-timeout=5m");

    @Test
    void defaultsUseWhenNoCustomBindingProperties() {
        this.contextRunner.run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(EventHubsExtendedBindingProperties.class);
            EventHubsExtendedBindingProperties properties = context.getBean(EventHubsExtendedBindingProperties.class);

            EventHubsConsumerProperties consumerProperties = properties.getExtendedConsumerProperties("process-in-0");
            assertEquals(consumerProperties.getCheckpoint().getMode(), CheckpointMode.BATCH);
            assertEquals(consumerProperties.getLoadBalancing().getStrategy(), LoadBalancingStrategy.BALANCED);
            assertEquals(consumerProperties.getBatch().getMaxSize(), 6);

            EventHubsProducerProperties producerProperties = properties.getExtendedProducerProperties("process-out-0");
            assertTrue(producerProperties.isSync());
            assertEquals(producerProperties.getSendTimeout(), Duration.ofMinutes(5));
        });
    }

    @Test
    void defaultsRespectedWhenCustomBindingProperties() {
        this.contextRunner
                .withPropertyValues(
                    "spring.cloud.stream.eventhubs.bindings.process-in-1.consumer.checkpoint.mode=manual",
                    "spring.cloud.stream.eventhubs.bindings.process-out-1.producer.sync=false")
            .run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(EventHubsExtendedBindingProperties.class);
                EventHubsExtendedBindingProperties properties = context.getBean(EventHubsExtendedBindingProperties.class);

                EventHubsConsumerProperties consumerProperties = properties.getExtendedConsumerProperties("process-in-1");
                assertEquals(consumerProperties.getCheckpoint().getMode(), CheckpointMode.MANUAL);
                assertEquals(consumerProperties.getLoadBalancing().getStrategy(), LoadBalancingStrategy.BALANCED);
                assertEquals(consumerProperties.getBatch().getMaxSize(), 6);

                EventHubsProducerProperties producerProperties = properties.getExtendedProducerProperties("process-out-1");
                assertFalse(producerProperties.isSync());
                assertEquals(producerProperties.getSendTimeout(), Duration.ofMinutes(5));
            });
    }

    @EnableAutoConfiguration
    static class TestApp {
    }
}
