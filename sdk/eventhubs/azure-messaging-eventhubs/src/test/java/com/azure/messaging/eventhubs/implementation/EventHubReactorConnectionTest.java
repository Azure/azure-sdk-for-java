// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.credential.TokenCredential;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @BeforeEach
    public void setup() throws IOException {
        final ConnectionHandler connectionHandler = new ConnectionHandler(CONNECTION_ID, HOSTNAME);

        MockitoAnnotations.initMocks(this);

        when(reactor.selectable()).thenReturn(selectable);
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(), connectionHandler))
            .thenReturn(reactorConnection);
        when(reactor.process()).thenReturn(true);

        final ProxyOptions proxy = ProxyOptions.SYSTEM_DEFAULTS;
        connectionOptions = new ConnectionOptions(HOSTNAME, "event-hub-name",
            tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP, new AmqpRetryOptions(),
            proxy, scheduler);

        final ReactorDispatcher reactorDispatcher = new ReactorDispatcher(reactor);
        when(reactorProvider.getReactor()).thenReturn(reactor);
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
        when(reactorProvider.createReactor(connectionHandler.getConnectionId(), connectionHandler.getMaxFrameSize()))
            .thenReturn(reactor);

        when(handlerProvider.createConnectionHandler(CONNECTION_ID, HOSTNAME, AmqpTransportType.AMQP, proxy))
            .thenReturn(connectionHandler);
    }

    @Test
    public void getsManagementChannel() {
        // Arrange
        final EventHubReactorAmqpConnection connection = new EventHubReactorAmqpConnection(CONNECTION_ID, connectionOptions,
            reactorProvider, handlerProvider, tokenManagerProvider, messageSerializer);

        // Act & Assert
        StepVerifier.create(connection.getManagementNode())
            .assertNext(node -> Assertions.assertTrue(node instanceof ManagementChannel))
            .verifyComplete();
    }

    @AfterEach
    public void teardown() {
        Mockito.framework().clearInlineMocks();
    }
}
