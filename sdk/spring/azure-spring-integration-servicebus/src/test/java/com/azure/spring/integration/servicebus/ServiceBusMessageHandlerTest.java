// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus;

import com.azure.spring.integration.core.DefaultMessageHandler;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.azure.spring.integration.test.support.MessageHandlerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.Message;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ServiceBusMessageHandlerTest extends MessageHandlerTest<ServiceBusQueueOperation> {

    @BeforeEach
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
