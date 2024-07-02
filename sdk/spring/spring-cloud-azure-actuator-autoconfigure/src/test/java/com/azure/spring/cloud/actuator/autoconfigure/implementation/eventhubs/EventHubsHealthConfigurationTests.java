// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.implementation.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.actuator.implementation.eventhubs.EventHubsHealthIndicator;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class EventHubsHealthConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues(
            "spring.cloud.azure.eventhubs.namespace=test-namespace",
            "spring.cloud.azure.eventhubs.event-hub-name=test-eventhus-name",
            "spring.cloud.azure.eventhubs.consumer.consumer-group=test-consumer-group",
            "spring.cloud.azure.eventhubs.consumer.namespace=test-namespace",
            "spring.cloud.azure.eventhubs.producer.namespace=test-namespace"
        )
        .withBean(AzureGlobalProperties.class)
        .withBean(EventHubClientBuilder.class)
        .withConfiguration(AutoConfigurations.of(AzureEventHubsAutoConfiguration.class,
            EventHubsHealthConfiguration.class));

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(EventHubsHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.azure-eventhubs.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(EventHubsHealthIndicator.class));
    }
}
