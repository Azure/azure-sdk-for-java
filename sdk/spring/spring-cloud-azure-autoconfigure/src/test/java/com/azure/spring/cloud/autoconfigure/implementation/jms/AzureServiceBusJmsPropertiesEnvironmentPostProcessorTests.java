// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AzureServiceBusJmsPropertiesEnvironmentPostProcessorTests {

    private final AzureServiceBusJmsPropertiesEnvironmentPostProcessor processor =
        new AzureServiceBusJmsPropertiesEnvironmentPostProcessor();
    private final MockEnvironment environment = new MockEnvironment();

    @Test
    void testPoolDefault() {
        processor.postProcessEnvironment(environment, null);

        assertFalse(environment.containsProperty("spring.jms.cache.enabled"));
        assertEquals("true", environment.getProperty("spring.jms.servicebus.pool.enabled"));
    }

    @Test
    void testCacheEnable() {
        environment.setProperty("spring.jms.cache.enabled", "true");
        processor.postProcessEnvironment(environment, null);

        assertEquals("true", environment.getProperty("spring.jms.cache.enabled"));
        assertFalse(environment.containsProperty("spring.jms.servicebus.pool.enabled"));
    }

    @Test
    void testCacheDisable() {
        environment.setProperty("spring.jms.cache.enabled", "false");
        processor.postProcessEnvironment(environment, null);

        assertEquals("false", environment.getProperty("spring.jms.cache.enabled"));
        assertFalse(environment.containsProperty("spring.jms.servicebus.pool.enabled"));
    }

    @Test
    void testPoolEnable() {
        environment.setProperty("spring.jms.servicebus.pool.enabled", "true");
        processor.postProcessEnvironment(environment, null);

        assertFalse(environment.containsProperty("spring.jms.cache.enabled"));
        assertEquals("true", environment.getProperty("spring.jms.servicebus.pool.enabled"));
    }

    @Test
    void testPoolDisable() {
        environment.setProperty("spring.jms.servicebus.pool.enabled", "false");
        processor.postProcessEnvironment(environment, null);

        assertFalse(environment.containsProperty("spring.jms.cache.enabled"));
        assertEquals("false", environment.getProperty("spring.jms.servicebus.pool.enabled"));
    }

    @Test
    void testPoolEnableCacheEnable() {
        environment.setProperty("spring.jms.cache.enabled", "true");
        environment.setProperty("spring.jms.servicebus.pool.enabled", "true");
        processor.postProcessEnvironment(environment, null);

        assertEquals("true", environment.getProperty("spring.jms.cache.enabled"));
        assertEquals("true", environment.getProperty("spring.jms.servicebus.pool.enabled"));
    }

    @Test
    void testPoolDisableCacheEnable() {
        environment.setProperty("spring.jms.cache.enabled", "true");
        environment.setProperty("spring.jms.servicebus.pool.enabled", "false");
        processor.postProcessEnvironment(environment, null);

        assertEquals("true", environment.getProperty("spring.jms.cache.enabled"));
        assertEquals("false", environment.getProperty("spring.jms.servicebus.pool.enabled"));
    }

    @Test
    void testPoolEnableCacheDisable() {
        environment.setProperty("spring.jms.cache.enabled", "false");
        environment.setProperty("spring.jms.servicebus.pool.enabled", "true");
        processor.postProcessEnvironment(environment, null);

        assertEquals("false", environment.getProperty("spring.jms.cache.enabled"));
        assertEquals("true", environment.getProperty("spring.jms.servicebus.pool.enabled"));
    }

    @Test
    void testPoolDisableCacheDisable() {
        environment.setProperty("spring.jms.cache.enabled", "false");
        environment.setProperty("spring.jms.servicebus.pool.enabled", "false");
        processor.postProcessEnvironment(environment, null);

        assertEquals("false", environment.getProperty("spring.jms.cache.enabled"));
        assertEquals("false", environment.getProperty("spring.jms.servicebus.pool.enabled"));
    }
}
