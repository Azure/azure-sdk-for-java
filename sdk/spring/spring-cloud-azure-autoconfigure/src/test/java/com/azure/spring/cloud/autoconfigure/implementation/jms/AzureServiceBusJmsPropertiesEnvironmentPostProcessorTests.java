// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzureServiceBusJmsPropertiesEnvironmentPostProcessorTests {

    private final AzureServiceBusJmsPropertiesEnvironmentPostProcessor processor =
        new AzureServiceBusJmsPropertiesEnvironmentPostProcessor();
    private final MockEnvironment environment = new MockEnvironment();

    @Test
    void testCacheEnabledPoolEnabled() {
        environment.setProperty("spring.jms.cache.enabled", "true");
        environment.setProperty("spring.jms.servicebus.pool.enabled", "true");
        assertThrows(IllegalStateException.class, () -> {
            processor.postProcessEnvironment(environment, null);
        }, "spring.jms.cache.enabled and spring.jms.servicebus.pool.enabled cannot be set at the same time.");
    }

    @Test
    void testCacheDisabledPoolEnabled() {
        environment.setProperty("spring.jms.cache.enabled", "false");
        environment.setProperty("spring.jms.servicebus.pool.enabled", "true");
        assertThrows(IllegalStateException.class, () -> {
            processor.postProcessEnvironment(environment, null);
        }, "spring.jms.cache.enabled and spring.jms.servicebus.pool.enabled cannot be set at the same time.");
    }

    @Test
    void testOnlyPoolDisabled() {
        environment.setProperty("spring.jms.servicebus.pool.enabled", "false");
        assertThrows(IllegalStateException.class, () -> {
            processor.postProcessEnvironment(environment, null);
        }, "spring.jms.cache.enabled must be set when spring.jms.servicebus.pool.enabled is set to false.");
    }

    @Test
    void testOnlyCacheEnabled() {
        environment.setProperty("spring.jms.cache.enabled", "true");
        processor.postProcessEnvironment(environment, null);
        assertEquals("false", environment.getProperty("spring.jms.servicebus.pool.enabled"));
    }

    @Test
    void testOnlyCacheDisabled() {
        environment.setProperty("spring.jms.cache.enabled", "true");
        processor.postProcessEnvironment(environment, null);
        assertEquals("false", environment.getProperty("spring.jms.servicebus.pool.enabled"));
    }
}
