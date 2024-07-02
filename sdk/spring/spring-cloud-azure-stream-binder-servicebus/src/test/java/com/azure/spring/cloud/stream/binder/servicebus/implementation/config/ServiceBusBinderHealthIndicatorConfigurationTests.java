// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.implementation.config;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.stream.binder.servicebus.implementation.ServiceBusHealthIndicator;
import com.azure.spring.cloud.stream.binder.servicebus.implementation.ServiceBusMessageChannelBinder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ServiceBusBinderHealthIndicatorConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AzureGlobalProperties.class, () -> mock(AzureGlobalProperties.class))
        .withBean(ServiceBusMessageChannelBinder.class, () -> mock(ServiceBusMessageChannelBinder.class))
        .withConfiguration(AutoConfigurations.of(ServiceBusBinderHealthIndicatorConfiguration.class));

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(ServiceBusHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.binders.enabled:false")
                          .run((context) -> assertThat(context).doesNotHaveBean(ServiceBusHealthIndicator.class));
    }
}
