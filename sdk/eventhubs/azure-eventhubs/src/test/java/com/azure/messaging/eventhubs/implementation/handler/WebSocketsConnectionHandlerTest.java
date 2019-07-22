// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.handler;

import com.azure.messaging.eventhubs.implementation.ClientConstants;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static com.azure.messaging.eventhubs.implementation.handler.ConnectionHandler.FRAMEWORK;
import static com.azure.messaging.eventhubs.implementation.handler.ConnectionHandler.PLATFORM;
import static com.azure.messaging.eventhubs.implementation.handler.ConnectionHandler.PRODUCT;
import static com.azure.messaging.eventhubs.implementation.handler.ConnectionHandler.VERSION;
import static com.azure.messaging.eventhubs.implementation.handler.WebSocketsConnectionHandler.HTTPS_PORT;
import static com.azure.messaging.eventhubs.implementation.handler.WebSocketsConnectionHandler.MAX_FRAME_SIZE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebSocketsConnectionHandlerTest {
    private static final String CONNECTION_ID = "some-random-id";
    private static final String HOSTNAME = "hostname-random";
    private WebSocketsConnectionHandler handler;

    @Captor
    ArgumentCaptor<Map<Symbol, Object>> argumentCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        handler = new WebSocketsConnectionHandler(CONNECTION_ID, HOSTNAME);
    }

    @After
    public void teardown() {
        Mockito.framework().clearInlineMocks();
        argumentCaptor = null;
    }

    @Test
    public void createHandler() {
        // Arrange
        final Map<String, String> expected = new HashMap<>();
        expected.put(PRODUCT.toString(), ClientConstants.PRODUCT_NAME);
        expected.put(VERSION.toString(), ClientConstants.CURRENT_JAVA_CLIENT_VERSION);
        expected.put(PLATFORM.toString(), ClientConstants.PLATFORM_INFO);
        expected.put(FRAMEWORK.toString(), ClientConstants.FRAMEWORK_INFO);

        // Assert
        Assert.assertEquals(HOSTNAME, handler.getHostname());
        Assert.assertEquals(MAX_FRAME_SIZE, handler.getMaxFrameSize());
        Assert.assertEquals(HTTPS_PORT, handler.getProtocolPort());

        final Map<String, Object> properties = handler.getConnectionProperties();
        expected.forEach((key, value) -> {
            Assert.assertTrue(properties.containsKey(key));

            final Object actual = properties.get(key);

            Assert.assertTrue(actual instanceof String);
            Assert.assertEquals(value, actual);
        });
    }

    @Test
    public void addsSslLayer() {
        // Arrange
        final TransportInternal transport = mock(TransportInternal.class);
        final Connection connection = mock(Connection.class);
        when(connection.getRemoteState()).thenReturn(EndpointState.CLOSED);

        final Event event = mock(Event.class);
        when(event.getTransport()).thenReturn(transport);
        when(event.getConnection()).thenReturn(connection);

        // Act
        handler.onConnectionBound(event);

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .then(handler::close)
            .verifyComplete();

        // Assert
        verify(transport, times(1)).ssl(any());
    }

    @Test
    public void onConnectionInit() {
        // Arrange
        final String expectedHostname = String.join(":", HOSTNAME, String.valueOf(HTTPS_PORT));
        final Map<String, Object> expectedProperties = new HashMap<>(handler.getConnectionProperties());
        final Connection connection = mock(Connection.class);
        final Event event = mock(Event.class);
        when(event.getConnection()).thenReturn(connection);

        // Act
        handler.onConnectionInit(event);

        // Assert
        verify(connection, times(1)).setHostname(expectedHostname);
        verify(connection, times(1)).setContainer(CONNECTION_ID);
        verify(connection, times(1)).open();

        verify(connection).setProperties(argumentCaptor.capture());
        Map<Symbol, Object> actualProperties = argumentCaptor.getValue();
        Assert.assertEquals(expectedProperties.size(), actualProperties.size());
        expectedProperties.forEach((key, value) -> {
            final Symbol symbol = Symbol.getSymbol(key);
            final Object removed = actualProperties.remove(symbol);
            Assert.assertNotNull(removed);

            final String expected = String.valueOf(value);
            final String actual = String.valueOf(removed);
            Assert.assertEquals(expected, actual);
        });
        Assert.assertTrue(actualProperties.isEmpty());
    }
}
