// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.servicebus.stream.binder.config;

import com.azure.spring.servicebus.core.topic.ServiceBusTopicOperation;
import com.azure.spring.servicebus.stream.binder.ServiceBusTopicHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for {@link ServiceBusTopicHealthIndicator}.
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
@ConditionalOnEnabledHealthIndicator("binders")
public class ServiceBusTopicBinderHealthIndicatorConfiguration {

    @Bean
    public ServiceBusTopicHealthIndicator serviceBusQueueHealthIndicator(ServiceBusTopicOperation serviceBusTopicOperation) {
        return new ServiceBusTopicHealthIndicator(serviceBusTopicOperation);
    }

}
