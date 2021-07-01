// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link StrictTlsContextSpi}.
 */
class StrictTlsContextSpiTest {

    @Mock
    private SSLContext sslContext;
    @Mock
    private SSLEngine sslEngine;
    @Captor
    private ArgumentCaptor<String[]> protocolsCaptor;

    private StrictTlsContextSpi contextSpi;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void beforeEach() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        contextSpi = new StrictTlsContextSpi(sslContext);
    }

    @AfterEach
    void teardown() throws Exception {
        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMocks();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    void constructorNull() {
        assertThrows(NullPointerException.class, () -> new StrictTlsContextSpi(null));
    }

    /**
     * Verify that SSLv2Hello protocol is removed.
     */
    @ValueSource(strings = {"SSLv2Hello", "Sslv2hello"})
    @ParameterizedTest
    void engineCreateSSLEngine(String protocol) {
        // Arrange
        final List<String> protocols = Arrays.asList("foo", protocol, "bar");

        when(sslContext.createSSLEngine()).thenReturn(sslEngine);
        when(sslEngine.getEnabledProtocols()).thenReturn(protocols.toArray(new String[0]));

        // Act
        final SSLEngine actual = contextSpi.engineCreateSSLEngine();

        // Assert
        assertEquals(sslEngine, actual);

        verify(sslEngine).setEnabledProtocols(protocolsCaptor.capture());
        final Set<String> actualProtocols = Stream.of(protocolsCaptor.getValue()).collect(Collectors.toSet());

        // Assert that the list is one smaller. (SSLv2Hello was removed.)
        assertEquals(protocols.size() - 1, actualProtocols.size());
        assertFalse(actualProtocols.contains(protocol));
    }

    /**
     * Verify that SSLv2Hello protocol is removed.
     */
    @ValueSource(strings = {"SSLv2Hello", "Sslv2hello"})
    @ParameterizedTest
    void engineCreateSSLEngineHostPort(String protocol) {
        // Arrange
        final String host = "fake-hostname.com";
        final int port = 1005;
        final List<String> protocols = Arrays.asList("foo", protocol, "bar");

        when(sslContext.createSSLEngine(host, port)).thenReturn(sslEngine);
        when(sslEngine.getEnabledProtocols()).thenReturn(protocols.toArray(new String[0]));

        // Act
        final SSLEngine actual = contextSpi.engineCreateSSLEngine(host, port);

        // Assert
        assertEquals(sslEngine, actual);

        verify(sslEngine).setEnabledProtocols(protocolsCaptor.capture());
        final Set<String> actualProtocols = Stream.of(protocolsCaptor.getValue()).collect(Collectors.toSet());

        // Assert that the list is one smaller. (SSLv2Hello was removed.)
        assertEquals(protocols.size() - 1, actualProtocols.size());
        assertFalse(actualProtocols.contains(protocol));
    }

    @Test
    void engineGetServerSessionContext() {
        // Arrange
        final SSLSessionContext expected = mock(SSLSessionContext.class);
        when(sslContext.getServerSessionContext()).thenReturn(expected);

        // Act
        final SSLSessionContext actual = contextSpi.engineGetServerSessionContext();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void engineGetClientSessionContext() {
        // Arrange
        final SSLSessionContext expected = mock(SSLSessionContext.class);
        when(sslContext.getClientSessionContext()).thenReturn(expected);

        // Act
        final SSLSessionContext actual = contextSpi.engineGetClientSessionContext();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void engineGetServerSocketFactory() {
        // Arrange
        final SSLServerSocketFactory expected = mock(SSLServerSocketFactory.class);
        when(sslContext.getServerSocketFactory()).thenReturn(expected);

        // Act
        final SSLServerSocketFactory actual = contextSpi.engineGetServerSocketFactory();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void engineGetSocketFactory() {
        // Arrange
        final SSLSocketFactory expected = mock(SSLSocketFactory.class);
        when(sslContext.getSocketFactory()).thenReturn(expected);

        // Act
        final SSLSocketFactory actual = contextSpi.engineGetSocketFactory();

        // Assert
        assertEquals(expected, actual);
    }

}
