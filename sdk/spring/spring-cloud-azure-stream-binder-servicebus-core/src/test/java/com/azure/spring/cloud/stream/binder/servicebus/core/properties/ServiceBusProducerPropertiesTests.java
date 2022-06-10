// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServiceBusProducerPropertiesTests {

    static final String CONNECTION_STRING = "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";
    private ServiceBusProducerProperties producerProperties;

    @BeforeEach
    void beforeEach() {
        producerProperties = new ServiceBusProducerProperties();
    }

    @Test
    void sendTimeoutDefaultsTo10000() {
        assertEquals(Duration.ofMillis(10000), producerProperties.getSendTimeout());
    }

    @Test
    void customSendTimeout() {
        Duration duration = Duration.ofSeconds(10);
        producerProperties.setSendTimeout(duration);
        assertEquals(duration, producerProperties.getSendTimeout());
    }

    @Test
    void syncDefaultsToFalse() {
        assertEquals(false, producerProperties.isSync());
    }

    @Test
    void customSync() {
        producerProperties.setSync(true);
        assertEquals(true, producerProperties.isSync());
    }

    @Test
    void domainNameDefaultsToFalse() {
        assertEquals("servicebus.windows.net", producerProperties.getDomainName());
    }

    @Test
    void customDomainName() {
        producerProperties.setDomainName("new.servicebus.windows.net");
        assertEquals("new.servicebus.windows.net", producerProperties.getDomainName());
    }

    @Test
    void getFqnWhenNamespaceIsNull() {
        producerProperties.setConnectionString(String.format(CONNECTION_STRING, "test"));
        assertEquals("test.servicebus.windows.net", producerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqnWhenNamespaceIsNotNull() {
        producerProperties.setNamespace("dev-namespace");
        assertEquals("dev-namespace.servicebus.windows.net", producerProperties.getFullyQualifiedNamespace());
    }

    @Test
    void getFqnReturnNullWhenNamespaceAndConnectionStringAreNull() {
        assertNull(producerProperties.getFullyQualifiedNamespace());
    }
}
