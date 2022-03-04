// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsHealthIndicator;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsMessageChannelBinder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class EventHubsBinderHealthIndicatorConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AzureGlobalProperties.class, () -> mock(AzureGlobalProperties.class))
        .withBean(EventHubsMessageChannelBinder.class, () -> mock(EventHubsMessageChannelBinder.class))
        .withConfiguration(AutoConfigurations.of(EventHubsBinderHealthIndicatorConfiguration.class));

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(EventHubsHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.binders.enabled:false")
                          .run((context) -> assertThat(context).doesNotHaveBean(EventHubsHealthIndicator.class));
    }
}
