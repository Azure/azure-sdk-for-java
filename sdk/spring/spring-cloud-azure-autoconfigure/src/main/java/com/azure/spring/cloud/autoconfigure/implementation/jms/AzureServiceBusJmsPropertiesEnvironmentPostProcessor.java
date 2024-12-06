// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

class AzureServiceBusJmsPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.containsProperty("spring.jms.servicebus.pool.enabled") && !environment.containsProperty("spring.jms.cache.enabled")) {
            Map<String, Object> azureServiceBusJms = new HashMap<>();
            azureServiceBusJms.put("spring.jms.servicebus.pool.enabled", "true");
            environment.getPropertySources().addFirst(new MapPropertySource("azureServiceBusJms", azureServiceBusJms));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
