// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsHealthIndicator;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsMessageChannelBinder;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for {@link EventHubsHealthIndicator}.
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
@ConditionalOnEnabledHealthIndicator("binders")
class EventHubsBinderHealthIndicatorConfiguration {

    @Bean
    EventHubsHealthIndicator eventhubsHealthIndicator(EventHubsMessageChannelBinder binder) {
        return new EventHubsHealthIndicator(binder);
    }
}
