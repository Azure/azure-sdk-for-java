// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.implementation.CBSAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.models.ProxyConfiguration;
import com.azure.core.credential.TokenCredential;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.mockito.Mockito.when;

public class EventHubReactorConnectionTest {
    private static final String CONNECTION_ID = "test-connection-id";
    private static final String HOSTNAME = "test-event-hub.servicebus.windows.net/";

    @Mock
    private Reactor reactor;
    @Mock
    private Selectable selectable;
    @Mock
    private TokenManagerProvider tokenManagerProvider;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Scheduler scheduler;
    @Mock
    private Connection reactorConnection;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private ReactorHandlerProvider handlerProvider;
    private ConnectionOptions connectionOptions;

    @Before
    public void setup() throws IOException {
        final ConnectionHandler connectionHandler = new ConnectionHandler(CONNECTION_ID, HOSTNAME);

        MockitoAnnotations.initMocks(this);

        when(reactor.selectable()).thenReturn(selectable);
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(), connectionHandler))
            .thenReturn(reactorConnection);
        when(reactor.process()).thenReturn(true);

        final ProxyConfiguration proxy = ProxyConfiguration.SYSTEM_DEFAULTS;
        connectionOptions = new ConnectionOptions(HOSTNAME, "event-hub-name",
            tokenCredential, CBSAuthorizationType.SHARED_ACCESS_SIGNATURE, TransportType.AMQP, new RetryOptions(),
            proxy, scheduler);

        final ReactorDispatcher reactorDispatcher = new ReactorDispatcher(reactor);
        when(reactorProvider.getReactor()).thenReturn(reactor);
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
        when(reactorProvider.createReactor(connectionHandler.getConnectionId(), connectionHandler.getMaxFrameSize()))
            .thenReturn(reactor);

        when(handlerProvider.createConnectionHandler(CONNECTION_ID, HOSTNAME, TransportType.AMQP, proxy))
            .thenReturn(connectionHandler);
    }

    @Test
    public void getsManagementChannel() {
        // Arrange
        final EventHubReactorConnection connection = new EventHubReactorConnection(CONNECTION_ID, connectionOptions,
            reactorProvider, handlerProvider, tokenManagerProvider, messageSerializer);

        // Act & Assert
        StepVerifier.create(connection.getManagementNode())
            .assertNext(node -> Assert.assertTrue(node instanceof ManagementChannel))
            .verifyComplete();
    }

    @After
    public void teardown() {
        Mockito.framework().clearInlineMocks();
    }
}
