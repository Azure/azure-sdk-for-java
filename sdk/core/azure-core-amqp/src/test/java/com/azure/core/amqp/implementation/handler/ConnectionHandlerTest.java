// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.implementation.ClientConstants;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.UserAgentUtil;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static com.azure.core.amqp.implementation.handler.ConnectionHandler.AMQPS_PORT;
import static com.azure.core.amqp.implementation.handler.ConnectionHandler.FRAMEWORK;
import static com.azure.core.amqp.implementation.handler.ConnectionHandler.MAX_FRAME_SIZE;
import static com.azure.core.amqp.implementation.handler.ConnectionHandler.PLATFORM;
import static com.azure.core.amqp.implementation.handler.ConnectionHandler.USER_AGENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionHandlerTest {
    private static final String APPLICATION_ID = "some-random-app-id";
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions().setApplicationId(APPLICATION_ID);
    private static final String CONNECTION_ID = "some-random-id";
    private static final String HOSTNAME = "hostname-random";
    private ConnectionHandler handler;
    private static final String PRODUCT = "test";
    private static final String CLIENT_VERSION = "1.0.0-test";
    private static final SslDomain.VerifyMode VERIFY_MODE = SslDomain.VerifyMode.VERIFY_PEER_NAME;

    @Captor
    private ArgumentCaptor<Map<Symbol, Object>> argumentCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        handler = new ConnectionHandler(CONNECTION_ID, HOSTNAME, PRODUCT, CLIENT_VERSION, VERIFY_MODE, CLIENT_OPTIONS);
    }

    @AfterEach
    public void teardown() {
        Mockito.framework().clearInlineMocks();
        argumentCaptor = null;
    }

    @Test
    void constructorNull() {
        assertThrows(NullPointerException.class, () -> new ConnectionHandler(
            null, HOSTNAME, PRODUCT, CLIENT_VERSION, VERIFY_MODE, CLIENT_OPTIONS));
        assertThrows(NullPointerException.class, () -> new ConnectionHandler(
            CONNECTION_ID, null, PRODUCT, CLIENT_VERSION, VERIFY_MODE, CLIENT_OPTIONS));
        assertThrows(NullPointerException.class, () -> new ConnectionHandler(
            CONNECTION_ID, HOSTNAME, null, CLIENT_VERSION, VERIFY_MODE, CLIENT_OPTIONS));
        assertThrows(NullPointerException.class, () -> new ConnectionHandler(
            CONNECTION_ID, HOSTNAME, PRODUCT, null, VERIFY_MODE, CLIENT_OPTIONS));
        assertThrows(NullPointerException.class, () -> new ConnectionHandler(
            CONNECTION_ID, HOSTNAME, PRODUCT, CLIENT_VERSION, null, CLIENT_OPTIONS));
        assertThrows(NullPointerException.class, () -> new ConnectionHandler(
            CONNECTION_ID, HOSTNAME, PRODUCT, CLIENT_VERSION, VERIFY_MODE, null));
    }

    @Test
    void applicationIdNotSet() {
        // Arrange
        final ClientOptions options = new ClientOptions();
        final String expected = UserAgentUtil.toUserAgentString(null, PRODUCT, CLIENT_VERSION, null);

        // Act
        final ConnectionHandler handler = new ConnectionHandler(CONNECTION_ID, HOSTNAME, PRODUCT, CLIENT_VERSION,
            VERIFY_MODE, options);

        // Assert
        final String userAgent = (String) handler.getConnectionProperties().get(USER_AGENT.toString());
        assertEquals(expected, userAgent);
    }

    @Test
    void applicationIdSet() {
        // Arrange
        final ClientOptions options = new ClientOptions().setApplicationId("my-application-id");
        final String expected = UserAgentUtil.toUserAgentString(options.getApplicationId(), PRODUCT, CLIENT_VERSION, null);

        // Act
        final ConnectionHandler handler = new ConnectionHandler(CONNECTION_ID, HOSTNAME, PRODUCT, CLIENT_VERSION,
            VERIFY_MODE, options);

        // Assert
        final String userAgent = (String) handler.getConnectionProperties().get(USER_AGENT.toString());
        assertEquals(expected, userAgent);
    }

    @Test
    void createHandler() {
        // Arrange
        final Map<String, String> expected = new HashMap<>();
        expected.put(PLATFORM.toString(), ClientConstants.PLATFORM_INFO);
        expected.put(FRAMEWORK.toString(), ClientConstants.FRAMEWORK_INFO);

        // Assert
        assertEquals(HOSTNAME, handler.getHostname());
        assertEquals(MAX_FRAME_SIZE, handler.getMaxFrameSize());
        assertEquals(AMQPS_PORT, handler.getProtocolPort());

        final Map<String, Object> properties = handler.getConnectionProperties();
        expected.forEach((key, value) -> {
            assertTrue(properties.containsKey(key));

            final Object actual = properties.get(key);

            assertTrue(actual instanceof String);
            assertEquals(value, actual);
        });

        assertTrue(properties.containsKey(USER_AGENT.toString()), "Expected the USER_AGENT string to be there.");

        final String actualUserAgent = (String) properties.get(USER_AGENT.toString());
        assertNotNull(actualUserAgent);
        assertEquals(0, actualUserAgent.indexOf(APPLICATION_ID), "Expected the APPLICATION_ID to be present in USER_AGENT.");
    }

    @Test
    void addsSslLayer() {
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
        verify(transport).ssl(any(SslDomain.class), any(SslPeerDetails.class));
    }

    @Test
    void onConnectionInit() {
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

        verify(connection).setProperties(argumentCaptor.capture());
        Map<Symbol, Object> actualProperties = argumentCaptor.getValue();
        assertEquals(expectedProperties.size(), actualProperties.size());
        expectedProperties.forEach((key, value) -> {
            final Symbol symbol = Symbol.getSymbol(key);
            final Object removed = actualProperties.remove(symbol);
            assertNotNull(removed);

            final String expected = String.valueOf(value);
            final String actual = String.valueOf(removed);
            assertEquals(expected, actual);
        });
        assertTrue(actualProperties.isEmpty());
    }
}
