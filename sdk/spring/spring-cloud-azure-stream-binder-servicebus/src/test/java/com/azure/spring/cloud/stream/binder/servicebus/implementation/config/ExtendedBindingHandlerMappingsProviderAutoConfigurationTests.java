// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.implementation.config;

import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusExtendedBindingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtendedBindingHandlerMappingsProviderAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ServiceBusBinderConfiguration.class))
        .withUserConfiguration(TestApp.class);

    @Test
    void testDefaultsConsumerBindingPropertiesWithNoCustom() {
        this.contextRunner
            .withPropertyValues("spring.cloud.stream.servicebus.default.consumer.entity-type=topic",
                "spring.cloud.stream.servicebus.default.consumer.requeue-rejected=true",
                "spring.cloud.stream.servicebus.default.consumer.auto-complete=false")
            .run(context -> {
                ServiceBusExtendedBindingProperties properties = context.getBean(ServiceBusExtendedBindingProperties.class);

                assertThat(properties.getExtendedConsumerProperties("process-in-0"))
                    .hasFieldOrPropertyWithValue("entityType", ServiceBusEntityType.TOPIC)
                    .hasFieldOrPropertyWithValue("requeueRejected", true)
                    .hasFieldOrPropertyWithValue("autoComplete", false);
            });
    }

    @Test
    void testDefaultsConsumerBindingPropertiesWithCustom() {
        this.contextRunner
            .withPropertyValues("spring.cloud.stream.servicebus.default.consumer.entity-type=topic",
                "spring.cloud.stream.servicebus.default.consumer.requeue-rejected=true",
                "spring.cloud.stream.servicebus.default.consumer.auto-complete=false",
                "spring.cloud.stream.servicebus.bindings.process-in-0.consumer.entity-type=queue")
            .run(context -> {
                ServiceBusExtendedBindingProperties properties =
                    context.getBean(ServiceBusExtendedBindingProperties.class);
                assertThat(properties.getExtendedConsumerProperties("process-in-0"))
                    .hasFieldOrPropertyWithValue("entityType", ServiceBusEntityType.QUEUE)
                    .hasFieldOrPropertyWithValue("requeueRejected", true)
                    .hasFieldOrPropertyWithValue("autoComplete", false);
            });
    }

    @Test
    void testDefaultsProducerBindingPropertiesWithNoCustom() {
        this.contextRunner
            .withPropertyValues("spring.cloud.stream.servicebus.default.producer.entity-type=topic",
                "spring.cloud.stream.servicebus.default.producer.sync=true")
            .run(context -> {
                ServiceBusExtendedBindingProperties properties =
                    context.getBean(ServiceBusExtendedBindingProperties.class);
                assertThat(properties.getExtendedProducerProperties("process-out-0"))
                    .hasFieldOrPropertyWithValue("entityType", ServiceBusEntityType.TOPIC)
                    .hasFieldOrPropertyWithValue("sync", true);
            });
    }

    @Test
    void testDefaultsProducerBindingPropertiesWithCustom() {
        this.contextRunner
            .withPropertyValues("spring.cloud.stream.servicebus.default.producer.entity-type=topic",
                "spring.cloud.stream.servicebus.default.producer.sync=true",
                "spring.cloud.stream.servicebus.bindings.process-out-0.producer.entity-type=queue")
            .run(context -> {
                ServiceBusExtendedBindingProperties properties =
                    context.getBean(ServiceBusExtendedBindingProperties.class);
                assertThat(properties.getExtendedProducerProperties("process-out-0"))
                    .hasFieldOrPropertyWithValue("entityType", ServiceBusEntityType.QUEUE)
                    .hasFieldOrPropertyWithValue("sync", true);
            });
    }

    @EnableAutoConfiguration
    static class TestApp {
    }
}
