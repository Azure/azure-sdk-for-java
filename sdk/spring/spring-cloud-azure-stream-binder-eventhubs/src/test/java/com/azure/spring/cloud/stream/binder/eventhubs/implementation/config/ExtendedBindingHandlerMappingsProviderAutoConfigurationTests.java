// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.implementation.config;

import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsProducerProperties;
import com.azure.spring.messaging.eventhubs.core.checkpoint.CheckpointMode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtendedBindingHandlerMappingsProviderAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(EventHubsBinderConfiguration.class))
        .withUserConfiguration(TestApp.class);

    @Test
    void testDefaultsConsumerBindingPropertiesWithNoCustom() {
        this.contextRunner
            .withPropertyValues("spring.cloud.stream.eventhubs.default.consumer.checkpoint.mode=batch",
                "spring.cloud.stream.eventhubs.default.consumer.load-balancing.strategy=balanced",
                "spring.cloud.stream.eventhubs.default.consumer.batch.max-size=6")
            .run(context -> {
                EventHubsExtendedBindingProperties properties = context.getBean(EventHubsExtendedBindingProperties.class);
                EventHubsConsumerProperties consumerProperties = properties.getExtendedConsumerProperties("process-in-0");

                assertEquals(consumerProperties.getCheckpoint().getMode(), CheckpointMode.BATCH);
                assertEquals(consumerProperties.getLoadBalancing().getStrategy(), LoadBalancingStrategy.BALANCED);
                assertEquals(consumerProperties.getBatch().getMaxSize(), 6);
            });
    }

    @Test
    void testDefaultsConsumerBindingPropertiesWithCustom() {
        this.contextRunner
            .withPropertyValues("spring.cloud.stream.eventhubs.default.consumer.checkpoint.mode=batch",
                "spring.cloud.stream.eventhubs.default.consumer.load-balancing.strategy=balanced",
                "spring.cloud.stream.eventhubs.default.consumer.batch.max-size=6",
                "spring.cloud.stream.eventhubs.bindings.process-in-0.consumer.checkpoint.mode=manual")
            .run(context -> {
                EventHubsExtendedBindingProperties properties = context.getBean(EventHubsExtendedBindingProperties.class);
                EventHubsConsumerProperties consumerProperties = properties.getExtendedConsumerProperties("process-in-0");

                assertEquals(consumerProperties.getCheckpoint().getMode(), CheckpointMode.MANUAL);
                assertEquals(consumerProperties.getLoadBalancing().getStrategy(), LoadBalancingStrategy.BALANCED);
                assertEquals(consumerProperties.getBatch().getMaxSize(), 6);
            });
    }

    @Test
    void testDefaultsProducerBindingPropertiesWithNoCustom() {
        this.contextRunner
            .withPropertyValues("spring.cloud.stream.eventhubs.default.producer.sync=true",
                "spring.cloud.stream.eventhubs.default.producer.send-timeout=5m")
            .run(context -> {
                EventHubsExtendedBindingProperties properties = context.getBean(EventHubsExtendedBindingProperties.class);
                EventHubsProducerProperties producerProperties = properties.getExtendedProducerProperties("process-out-0");

                assertTrue(producerProperties.isSync());
                assertEquals(producerProperties.getSendTimeout(), Duration.ofMinutes(5));
            });
    }

    @Test
    void testDefaultsProducerBindingPropertiesWithCustom() {
        this.contextRunner
            .withPropertyValues("spring.cloud.stream.eventhubs.default.producer.sync=true",
                "spring.cloud.stream.eventhubs.default.producer.send-timeout=5m",
                "spring.cloud.stream.eventhubs.bindings.process-out-0.producer.sync=false")
            .run(context -> {
                EventHubsExtendedBindingProperties properties = context.getBean(EventHubsExtendedBindingProperties.class);
                EventHubsProducerProperties producerProperties = properties.getExtendedProducerProperties("process-out-0");

                assertFalse(producerProperties.isSync());
                assertEquals(producerProperties.getSendTimeout(), Duration.ofMinutes(5));
            });
    }

    @EnableAutoConfiguration
    static class TestApp {
    }
}
