// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.api;

import com.azure.spring.cloud.service.implementation.identity.AzureAuthenticationTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureAuthenticationTemplateTest {

    private static final String IS_INITIALIZED = "isInitialized";

    @Test
    void initCalledOnlyOnce() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        AtomicBoolean isInitialized = (AtomicBoolean) ReflectionTestUtils.getField(template, IS_INITIALIZED);
        assertFalse(isInitialized.get());
        Properties properties = new Properties();
        template.init(properties);
        assertTrue(isInitialized.get());
        assertFalse(isInitialized.compareAndSet(false, true));
    }

    @Test
    void shouldCallInit() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        AtomicBoolean isInitialized = (AtomicBoolean) ReflectionTestUtils.getField(template, "isInitialized");
        assertFalse(isInitialized.get());
        Properties properties = new Properties();
        template.init(properties);
        assertTrue(isInitialized.get());
    }

    @Test
    void mustCallInit() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        assertThrows(IllegalStateException.class, () -> template.getTokenAsPasswordAsync());
    }

    @Test
    void testCallInit() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        Properties properties = new Properties();
        template.init(properties);
        assertNotNull(template.getTokenAsPasswordAsync());
    }

    @Test
    void testBlockTimeout() {
        AzureAuthenticationTemplate template = new AzureAuthenticationTemplate();
        Duration blockTimeout = template.getBlockTimeout();
        assertEquals(blockTimeout, Duration.ofSeconds(30));
    }
}
