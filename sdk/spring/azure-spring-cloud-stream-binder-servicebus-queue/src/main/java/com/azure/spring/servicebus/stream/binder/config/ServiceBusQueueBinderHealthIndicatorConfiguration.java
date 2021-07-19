// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.servicebus.stream.binder.config;

import com.azure.spring.integration.servicebus.health.InstrumentationManager;
import com.azure.spring.servicebus.stream.binder.ServiceBusQueueHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
@ConditionalOnEnabledHealthIndicator("binders")
public class ServiceBusQueueBinderHealthIndicatorConfiguration {

    @Bean
    public ServiceBusQueueHealthIndicator serviceBusQueueHealthIndicator(InstrumentationManager instrumentationManager) {
        return new ServiceBusQueueHealthIndicator(instrumentationManager);
    }

}
