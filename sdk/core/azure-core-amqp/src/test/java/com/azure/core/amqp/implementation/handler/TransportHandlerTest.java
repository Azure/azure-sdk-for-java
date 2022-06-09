// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests transport handler.
 */
public class TransportHandlerTest {
    @Mock
    private Transport transport;
    @Mock
    private Connection connection;
    @Mock
    private Event event;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void beforeEach() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (autoCloseable != null) {
            autoCloseable.close();
        }
    }

    /**
     * Unbinds transport if there is one associated with the connection.
     */
    @Test
    public void unbindsTransport() {
        // Arrange
        when(event.getConnection()).thenReturn(connection);
        when(event.getTransport()).thenReturn(transport);

        when(connection.getTransport()).thenReturn(transport);

        final TransportHandler handler = new TransportHandler("name");

        // Act
        handler.onTransportClosed(event);

        // Assert
        verify(transport).unbind();
    }

    @Test
    public void noTransport() {
        // Arrange
        when(event.getTransport()).thenReturn(transport);
        when(event.getConnection()).thenReturn(connection);

        final TransportHandler handler = new TransportHandler("name");

        // Act
        handler.onTransportClosed(event);

        // Assert
        verify(transport, never()).unbind();
    }
}
