// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import java.io.IOException;

import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.NonSessionProcessor.RollingMessagePump;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NonSessionProcessorRollingMessagePumpTest {
    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void setup() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void beginAfterDisposeThrows() {
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(builder.buildAsyncClient()).thenReturn(client);
        when(client.nonSessionProcessorReceiveV2()).thenReturn(Flux.never());

        final RollingMessagePump pump = new RollingMessagePump(builder, m -> { }, e -> { }, 1, false);
        pump.dispose();

        assertThrows(IllegalStateException.class, () -> pump.begin());
    }

    @Test
    public void invokingBeginMoreThanOnceThrows() {
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(builder.buildAsyncClient()).thenReturn(client);
        when(client.nonSessionProcessorReceiveV2()).thenReturn(Flux.never());

        final RollingMessagePump pump = new RollingMessagePump(builder, m -> { }, e -> { }, 1, false);
        pump.begin();

        try {
            assertThrows(IllegalStateException.class, () -> pump.begin());
        } finally {
            pump.dispose();
        }
    }

    // This is to assert that when RollingMessagePump rolls to the next MessagePump, the cancellation internal to
    // the RollingMessagePump will close the client associated with the previous MessagePump.
    @Test
    public void cancelClosesClient() {
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(builder.buildAsyncClient()).thenReturn(client);
        when(client.nonSessionProcessorReceiveV2()).thenReturn(Flux.never());
        doNothing().when(client).close();

        final RollingMessagePump pump = new RollingMessagePump(builder, m -> { }, e -> { }, 1, false);

        StepVerifier.create(pump.beginIntern())
            .thenAwait()
            .thenCancel()
            .verify();

        verify(client).close();
    }
}
