// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.spring.integration.core.DefaultMessageHandler;
import com.microsoft.azure.spring.integration.core.api.PartitionSupplier;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.microsoft.azure.spring.integration.test.support.MessageHandlerTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusMessageHandlerTest extends MessageHandlerTest<ServiceBusQueueOperation> {

    @Before
    @Override
    @SuppressWarnings("unchecked")
    public void setUp() {
        this.future.complete(null);
        this.sendOperation = mock(ServiceBusQueueOperation.class);
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class), isA(PartitionSupplier.class)))
            .thenReturn(future);
        when(this.sendOperation
            .sendAsync(eq(this.dynamicDestination), isA(Message.class), isA(PartitionSupplier.class)))
            .thenReturn(future);
        this.handler = new DefaultMessageHandler(this.destination, this.sendOperation);
    }
}
