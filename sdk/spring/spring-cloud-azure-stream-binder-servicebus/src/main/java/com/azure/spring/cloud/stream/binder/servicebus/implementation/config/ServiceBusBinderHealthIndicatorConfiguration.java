// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.stream.binder.servicebus.implementation.config;

import com.azure.spring.cloud.stream.binder.servicebus.implementation.ServiceBusHealthIndicator;
import com.azure.spring.cloud.stream.binder.servicebus.implementation.ServiceBusMessageChannelBinder;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for {@link ServiceBusHealthIndicator}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
@ConditionalOnEnabledHealthIndicator("binders")
public class ServiceBusBinderHealthIndicatorConfiguration {

    /**
     * Declare Service Bus Health Indicator bean.
     *
     * @param binder the binder
     * @return ServiceBusHealthIndicator bean the Service Bus Health Indicator bean
     */
    @Bean
    ServiceBusHealthIndicator serviceBusHealthIndicator(ServiceBusMessageChannelBinder binder) {
        return new ServiceBusHealthIndicator(binder);
    }


}
