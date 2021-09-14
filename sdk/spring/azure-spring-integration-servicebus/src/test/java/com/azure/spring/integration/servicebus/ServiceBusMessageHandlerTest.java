// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus;

import com.azure.spring.integration.core.DefaultMessageHandler;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueOperation;
import com.azure.spring.integration.test.support.MessageHandlerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceBusMessageHandlerTest extends MessageHandlerTest<ServiceBusQueueOperation> {


    private AutoCloseable closeable;


    @BeforeEach
    @Override
    @SuppressWarnings("unchecked")
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.future.complete(null);
        this.sendOperation = mock(ServiceBusQueueOperation.class);
        when(this.sendOperation.sendAsync(eq(this.destination), isA(Message.class),
                                          isA(PartitionSupplier.class))).thenReturn(future);
        when(
            this.sendOperation.sendAsync(eq(this.dynamicDestination), isA(Message.class), isA(PartitionSupplier.class)))
            .thenReturn(future);
        this.handler = new DefaultMessageHandler(this.destination, this.sendOperation);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

}
