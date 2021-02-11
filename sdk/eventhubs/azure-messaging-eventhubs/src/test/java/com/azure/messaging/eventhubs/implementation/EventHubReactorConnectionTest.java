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
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventHubReactorConnectionTest {
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    private static final String CONNECTION_ID = "test-connection-id";
    private static final String HOSTNAME = "test-event-hub.servicebus.windows.net/";

    private static String product;
    private static String clientVersion;

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
    @Mock
    private Session session;
    @Mock
    private Record record;

    private ConnectionOptions connectionOptions;

    @BeforeAll
    public static void init() {
        Map<String, String> properties = CoreUtils.getProperties("azure-messaging-eventhubs.properties");
        product = properties.get("name");
        clientVersion = properties.get("version");
    }

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        final ProxyOptions proxy = ProxyOptions.SYSTEM_DEFAULTS;
        this.connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP, new AmqpRetryOptions(), proxy,
            scheduler, CLIENT_OPTIONS, SslDomain.VerifyMode.VERIFY_PEER_NAME);

        final ConnectionHandler connectionHandler = new ConnectionHandler(CONNECTION_ID, product, clientVersion,
            connectionOptions);

        when(reactor.selectable()).thenReturn(selectable);
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(),
            connectionHandler))
            .thenReturn(reactorConnection);
        when(reactor.process()).thenReturn(true);
        when(reactor.attachments()).thenReturn(record);

        final ReactorDispatcher reactorDispatcher = new ReactorDispatcher(reactor);
        when(reactorProvider.getReactor()).thenReturn(reactor);
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
        when(reactorProvider.createReactor(connectionHandler.getConnectionId(), connectionHandler.getMaxFrameSize()))
            .thenReturn(reactor);

        final SessionHandler sessionHandler = new SessionHandler(CONNECTION_ID, HOSTNAME, "EVENT_HUB",
            reactorDispatcher, Duration.ofSeconds(20));

        when(handlerProvider.createConnectionHandler(CONNECTION_ID, product, clientVersion, connectionOptions))
            .thenReturn(connectionHandler);
        when(handlerProvider.createSessionHandler(eq(CONNECTION_ID), eq(HOSTNAME), anyString(), any(Duration.class)))
            .thenReturn(sessionHandler);

        when(reactorConnection.session()).thenReturn(session);
        final Record record = mock(Record.class);
        when(session.attachments()).thenReturn(record);
    }

    @AfterEach
    public void reset() {
        Mockito.reset(reactor, selectable, tokenManagerProvider, reactorConnection, messageSerializer, reactorProvider,
            handlerProvider, tokenCredential, scheduler);
    }

    @Test
    public void getsManagementChannel() {
        // Arrange
        final Sender sender = mock(Sender.class);
        final Receiver receiver = mock(Receiver.class);
        final Record linkRecord = mock(Record.class);
        when(session.sender(any())).thenReturn(sender);
        when(session.receiver(any())).thenReturn(receiver);

        when(sender.attachments()).thenReturn(linkRecord);
        when(receiver.attachments()).thenReturn(linkRecord);

        when(handlerProvider.createReceiveLinkHandler(eq(CONNECTION_ID), eq(HOSTNAME), anyString(), anyString()))
            .thenReturn(new ReceiveLinkHandler(CONNECTION_ID, HOSTNAME, "receiver-name", "test-entity-path"));

        when(handlerProvider.createSendLinkHandler(eq(CONNECTION_ID), eq(HOSTNAME), anyString(), anyString()))
            .thenReturn(new SendLinkHandler(CONNECTION_ID, HOSTNAME, "sender-name", "test-entity-path"));

        final EventHubReactorAmqpConnection connection = new EventHubReactorAmqpConnection(CONNECTION_ID,
            connectionOptions, "event-hub-name", reactorProvider, handlerProvider, tokenManagerProvider,
            messageSerializer, product, clientVersion);

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
