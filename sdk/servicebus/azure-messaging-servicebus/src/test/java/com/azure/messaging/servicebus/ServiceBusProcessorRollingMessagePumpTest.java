// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.ServiceBusProcessor.RollingMessagePump;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceBusProcessorRollingMessagePumpTest {
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
    public void shouldThrowIfBeginAfterDisposal() {
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(builder.buildAsyncClientForProcessor()).thenReturn(client);
        when(client.nonSessionProcessorReceiveV2()).thenReturn(Flux.never());

        final RollingMessagePump pump = new RollingMessagePump(builder, m -> {
        }, e -> {
        }, 1, false);

        pump.dispose();
        assertThrows(IllegalStateException.class, () -> pump.begin());
    }

    @Test
    public void shouldThrowIfBeginMoreThanOnce() {
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(builder.buildAsyncClientForProcessor()).thenReturn(client);
        when(client.nonSessionProcessorReceiveV2()).thenReturn(Flux.never());

        final RollingMessagePump pump = new RollingMessagePump(builder, m -> {
        }, e -> {
        }, 1, false);

        pump.begin();
        try {
            assertThrows(IllegalStateException.class, () -> pump.begin());
        } finally {
            pump.dispose();
        }
    }

    // This is to assert that when RollingMessagePump rolls to the next NonSessionMessagePump, the cancellation internal
    // to the RollingMessagePump will close the client associated with the previous NonSessionMessagePump.
    @Test
    public void shouldCloseClientOnCancel() {
        final ServiceBusReceiverClientBuilder builder = mock(ServiceBusReceiverClientBuilder.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);

        when(builder.buildAsyncClientForProcessor()).thenReturn(client);
        when(client.nonSessionProcessorReceiveV2()).thenReturn(Flux.never());
        doNothing().when(client).close();

        final RollingMessagePump pump = new RollingMessagePump(builder, m -> {
        }, e -> {
        }, 1, false);

        StepVerifier.create(pump.beginIntern()).thenAwait().thenCancel().verify();

        verify(client).close();
    }
}
