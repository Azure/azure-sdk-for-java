// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceBusConsumerPropertiesTests {

    private ServiceBusConsumerProperties consumerProperties;

    @BeforeEach
    void beforeEach() {
        consumerProperties = new ServiceBusConsumerProperties();
    }

    @Test
    void autoCompleteDefaultTrue() {
        assertTrue(consumerProperties.getAutoComplete());
    }

    @Test
    void customizeAutoComplete() {
        consumerProperties.setAutoComplete(false);
        assertFalse(consumerProperties.getAutoComplete());
    }

    @Test
    void requeueRejectedDefaultsToFalse() {
        assertFalse(consumerProperties.isRequeueRejected());
    }

    @Test
    void customRequeueRejected() {
        consumerProperties.setRequeueRejected(true);
        assertTrue(consumerProperties.isRequeueRejected());
    }

    @Test
    void maxConcurrentCallsDefaults() {
        assertEquals(1, consumerProperties.getMaxConcurrentCalls());
    }

    @Test
    void customMaxConcurrentCalls() {
        consumerProperties.setMaxConcurrentCalls(10);
        assertEquals(10, consumerProperties.getMaxConcurrentCalls());
    }

    @Test
    void maxConcurrentSessionsDefaults() {
        assertNull(consumerProperties.getMaxConcurrentSessions());
    }

    @Test
    void customMaxConcurrentSessions() {
        consumerProperties.setMaxConcurrentSessions(10);
        assertEquals(10, consumerProperties.getMaxConcurrentSessions());
    }

    @Test
    void subQueueDefaults() {
        assertNotNull(consumerProperties.getSubQueue());
    }

    @Test
    void customSubQueue() {
        consumerProperties.setSubQueue(SubQueue.DEAD_LETTER_QUEUE);
        assertEquals(SubQueue.DEAD_LETTER_QUEUE, consumerProperties.getSubQueue());
    }

    @Test
    void receiveModeDefaults() {
        assertEquals(ServiceBusReceiveMode.PEEK_LOCK, consumerProperties.getReceiveMode());
    }

    @Test
    void customReceiveMode() {
        consumerProperties.setReceiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE);
        assertEquals(ServiceBusReceiveMode.RECEIVE_AND_DELETE, consumerProperties.getReceiveMode());
    }

    @Test
    void maxAutoLockRenewDurationDefaults() {
        assertEquals(Duration.ofMinutes(5), consumerProperties.getMaxAutoLockRenewDuration());
    }

    @Test
    void customMaxAutoLockRenewDuration() {
        Duration duration = Duration.ofMinutes(6);
        consumerProperties.setMaxAutoLockRenewDuration(duration);
        assertEquals(duration, consumerProperties.getMaxAutoLockRenewDuration());
    }
}
