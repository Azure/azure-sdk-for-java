// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.config;

import com.azure.spring.eventhub.stream.binder.EventHubHealthIndicator;
import com.azure.spring.eventhub.stream.binder.EventHubMessageChannelBinder;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
@ConditionalOnEnabledHealthIndicator("binders")
class EventHubBinderHealthIndicatorConfiguration {

    @Bean
    EventHubHealthIndicator eventHubHealthIndicator(EventHubMessageChannelBinder binder,
                                                    EventHubClientFactory clientFactory) {
        return new EventHubHealthIndicator(binder, clientFactory);
    }


}
