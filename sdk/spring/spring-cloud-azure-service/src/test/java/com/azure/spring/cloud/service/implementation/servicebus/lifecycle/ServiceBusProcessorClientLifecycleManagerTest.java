// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.servicebus.lifecycle;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ServiceBusProcessorClientLifecycleManagerTest {

    private final ServiceBusProcessorClient processor = mock(ServiceBusProcessorClient.class);
    private final ServiceBusProcessorClientLifecycleManager manager = new ServiceBusProcessorClientLifecycleManager(processor);

    @Test
    void testStarting() {
        manager.start();
        verify(processor, times(1)).start();
    }

    @Test
    void testStopping() {
        manager.stop();
        verify(processor, times(1)).stop();
    }

    @Test
    void testIsRunning() {
        manager.isRunning();
        verify(processor, times(1)).isRunning();
    }
}
