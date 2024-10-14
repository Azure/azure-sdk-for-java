// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

class AzureServiceBusJmsPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 3;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.containsProperty("spring.jms.cache.enabled")) {
            if (environment.containsProperty("spring.jms.servicebus.pool.enabled") && environment.getProperty("spring"
                + ".jms.servicebus.pool.enabled").equals("true")) {
                throw new IllegalStateException("spring.jms.cache.enabled and spring.jms.servicebus.pool.enabled "
                    + "cannot be set at the same time.");
            }
            Map<String, Object> servicebusJmsMode = new HashMap<>();
            servicebusJmsMode.put("spring.jms.servicebus.pool.enabled", "false");
            environment.getPropertySources().addFirst(new MapPropertySource("servicebusJmsMode", servicebusJmsMode));
        } else if (environment.containsProperty("spring.jms.servicebus.pool.enabled") && environment.getProperty(
            "spring.jms.servicebus.pool.enabled").equals("false")) {
            throw new IllegalStateException("spring.jms.cache.enabled must be set when spring.jms.servicebus.pool"
                + ".enabled is set to false.");
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
