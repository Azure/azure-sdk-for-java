// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.credentials.TokenCredential;
import com.azure.messaging.eventhubs.models.ProxyConfiguration;
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

    private ReactorHandlerProvider handlerProvider;
    private ReactorProvider reactorProvider;
    private ConnectionOptions connectionOptions;

    @Before
    public void setup() throws IOException {
        final ConnectionHandler connectionHandler = new ConnectionHandler(CONNECTION_ID, HOSTNAME);

        MockitoAnnotations.initMocks(this);

        when(reactor.selectable()).thenReturn(selectable);
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(), connectionHandler))
            .thenReturn(reactorConnection);
        when(reactor.process()).thenReturn(true);

        connectionOptions = new ConnectionOptions(HOSTNAME, "event-hub-name",
            tokenCredential, CBSAuthorizationType.SHARED_ACCESS_SIGNATURE, TransportType.AMQP, new RetryOptions(),
            ProxyConfiguration.SYSTEM_DEFAULTS, scheduler);

        final ReactorDispatcher reactorDispatcher = new ReactorDispatcher(reactor);
        reactorProvider = new MockReactorProvider(reactor, reactorDispatcher);
        handlerProvider = new MockReactorHandlerProvider(reactorProvider, connectionHandler,
            null, null, null);
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
