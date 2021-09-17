// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.spring.cloud.stream.binder.eventhubs.EventHubHealthIndicator;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubMessageChannelBinder;
import com.azure.spring.eventhubs.core.EventHubClientFactory;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnEnabledHealthIndicator("binders")
class EventHubBinderHealthIndicatorConfiguration {

    @Bean
    EventHubHealthIndicator eventHubHealthIndicator(EventHubMessageChannelBinder binder,
                                                    EventHubClientFactory clientFactory) {
        return new EventHubHealthIndicator(binder, clientFactory);
    }


}
