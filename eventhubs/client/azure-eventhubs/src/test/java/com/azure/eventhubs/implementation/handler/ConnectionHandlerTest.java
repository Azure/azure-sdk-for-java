package com.azure.eventhubs.implementation.handler;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static com.azure.eventhubs.implementation.handler.ConnectionHandler.AMQPS_PORT;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.FRAMEWORK;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.MAX_FRAME_SIZE;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.PLATFORM;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.PRODUCT;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.USER_AGENT;
import static com.azure.eventhubs.implementation.handler.ConnectionHandler.VERSION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionHandlerTest {
    private static final String CONNECTION_ID = "some-random-id";
    private static final String HOSTNAME = "hostname-random";
    private ConnectionHandler handler;

    @Before
    public void setup() {
        handler = new ConnectionHandler(CONNECTION_ID, HOSTNAME);
    }

    @Test
    public void createHandler() {
        // Assert
        Assert.assertEquals(HOSTNAME, handler.getHostname());
        Assert.assertEquals(MAX_FRAME_SIZE, handler.getMaxFrameSize());
        Assert.assertEquals(AMQPS_PORT, handler.getProtocolPort());

        final Map<String, Object> properties = handler.getConnectionProperties();
        Assert.assertTrue(properties.containsKey(PRODUCT.toString()));
        Assert.assertTrue(properties.containsKey(VERSION.toString()));
        Assert.assertTrue(properties.containsKey(PLATFORM.toString()));
        Assert.assertTrue(properties.containsKey(FRAMEWORK.toString()));
        Assert.assertTrue(properties.containsKey(USER_AGENT.toString()));
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


    @SuppressWarnings("unchecked")
    @Test
    public void onConnectionInit() {
        // Arrange
        final String expectedHostname = String.join(":", HOSTNAME, String.valueOf(AMQPS_PORT));
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

        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
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
