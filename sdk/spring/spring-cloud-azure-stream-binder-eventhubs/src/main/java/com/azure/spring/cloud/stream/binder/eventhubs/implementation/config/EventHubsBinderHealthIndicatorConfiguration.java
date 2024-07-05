// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.implementation.config;

import com.azure.spring.cloud.stream.binder.eventhubs.implementation.EventHubsHealthIndicator;
import com.azure.spring.cloud.stream.binder.eventhubs.implementation.EventHubsMessageChannelBinder;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for {@link EventHubsHealthIndicator}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
@ConditionalOnEnabledHealthIndicator("binders")
class EventHubsBinderHealthIndicatorConfiguration {

    /**
     * Declare Event Hubs Health Indicator bean.
     *
     * @param binder the binder
     * @return EventHubsHealthIndicator bean the Event Hubs Health Indicator bean
     */
    @Bean
    EventHubsHealthIndicator eventhubsHealthIndicator(EventHubsMessageChannelBinder binder) {
        return new EventHubsHealthIndicator(binder);
    }
}
