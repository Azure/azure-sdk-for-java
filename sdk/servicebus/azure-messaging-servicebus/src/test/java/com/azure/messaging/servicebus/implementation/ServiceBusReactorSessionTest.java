// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Test for {@link ServiceBusReactorSession}.
 */
public class ServiceBusReactorSessionTest {
    private final ClientLogger logger = new ClientLogger(ServiceBusReactorSessionTest.class);

    private static final String CONNECTION_ID = "test-connection-id";
    private static final String HOSTNAME = "test-event-hub.servicebus.windows.net/";
    private static final Symbol LINK_TRANSFER_DESTINATION_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR
        + ":transfer-destination-address");

    @Mock
    private Reactor reactor;
    @Mock
    private Selectable selectable;
    @Mock
    private TokenManagerProvider tokenManagerProvider;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private ReactorHandlerProvider handlerProvider;
    @Mock
    private Session session;
    @Mock
    Mono<ClaimsBasedSecurityNode> cbsNodeSupplier;
    @Mock
    AmqpRetryPolicy retryPolicy;
    @Mock
    TokenManager tokenManagerViaQueue;
    @Mock
    TokenManager tokenManagerEntity;
    @Mock
    SessionHandler handler;
    @Mock
    Sender senderEntity;
    @Mock
    Sender senderViaEntity;
    @Mock
    Record record;
    @Mock
    private SendLinkHandler sendViaEntityLinkHandler;
    @Mock
    private SendLinkHandler sendEntityLinkHandler;
    @Captor
    private ArgumentCaptor<Runnable> dispatcherCaptor;
    @Mock
    private ReactorDispatcher dispatcher;

    private ServiceBusReactorSession serviceBusReactorSession;
    private String sessionName = "sessionName";
    private String entityPath = "entityPath";
    private String viaEntityPath = "viaEntityPath";
    private String viaEntityPathSenderLinkName = "VIA-" + viaEntityPath;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(60));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup(TestInfo testInfo) throws IOException {
        logger.info("[{}] Setting up.", testInfo.getDisplayName());

        MockitoAnnotations.initMocks(this);
        when(tokenManagerEntity.getAuthorizationResults()).thenReturn(Flux.just(AmqpResponseCode.ACCEPTED));
        when(tokenManagerViaQueue.getAuthorizationResults()).thenReturn(Flux.just(AmqpResponseCode.ACCEPTED));

        doNothing().when(selectable).setChannel(any());
        doNothing().when(selectable).onReadable(any());
        doNothing().when(selectable).onFree(any());
        doNothing().when(selectable).setReading(true);
        doNothing().when(reactor).update(selectable);
        when(reactor.selectable()).thenReturn(selectable);

        final ReplayProcessor<EndpointState> endpointStateReplayProcessor = ReplayProcessor.cacheLast();
        when(handler.getEndpointStates()).thenReturn(endpointStateReplayProcessor);
        FluxSink<EndpointState> sink1 = endpointStateReplayProcessor.sink();
        sink1.next(EndpointState.ACTIVE);
        when(handler.getHostname()).thenReturn(HOSTNAME);
        when(handler.getConnectionId()).thenReturn(CONNECTION_ID);
        when(handler.getErrors()).thenReturn(Flux.empty());

        when(handlerProvider.createSendLinkHandler(CONNECTION_ID, HOSTNAME, viaEntityPathSenderLinkName, viaEntityPath))
            .thenReturn(sendViaEntityLinkHandler);
        when(handlerProvider.createSendLinkHandler(CONNECTION_ID, HOSTNAME, entityPath, entityPath))
            .thenReturn(sendEntityLinkHandler);

        Delivery delivery = mock(Delivery.class);
        when(delivery.getRemoteState()).thenReturn(Accepted.getInstance());
        when(delivery.getTag()).thenReturn("tag".getBytes());
        when(sendViaEntityLinkHandler.getDeliveredMessages()).thenReturn(Flux.just(delivery));
        when(sendEntityLinkHandler.getDeliveredMessages()).thenReturn(Flux.just(delivery));

        when(sendViaEntityLinkHandler.getLinkCredits()).thenReturn(Flux.just(100));
        when(sendEntityLinkHandler.getLinkCredits()).thenReturn(Flux.just(100));

        when(sendViaEntityLinkHandler.getEndpointStates()).thenReturn(endpointStateReplayProcessor);
        when(sendEntityLinkHandler.getEndpointStates()).thenReturn(endpointStateReplayProcessor);

        when(sendViaEntityLinkHandler.getErrors()).thenReturn(Flux.empty());
        when(sendEntityLinkHandler.getErrors()).thenReturn(Flux.empty());

        when(tokenManagerProvider.getTokenManager(cbsNodeSupplier, viaEntityPath)).thenReturn(tokenManagerViaQueue);
        when(tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath)).thenReturn(tokenManagerEntity);

        when(tokenManagerEntity.getAuthorizationResults()).thenReturn(Flux.just(AmqpResponseCode.ACCEPTED));
        when(tokenManagerEntity.authorize()).thenReturn(Mono.just(1L));
        when(tokenManagerViaQueue.authorize()).thenReturn(Mono.just(1L));

        when(session.sender(viaEntityPathSenderLinkName)).thenReturn(senderViaEntity);
        when(session.sender(entityPath)).thenReturn(senderEntity);
        doNothing().when(session).open();
        doNothing().when(senderViaEntity).setSource(any(Source.class));
        doNothing().when(senderEntity).setSource(any(Source.class));

        doNothing().when(senderViaEntity).setSenderSettleMode(SenderSettleMode.UNSETTLED);
        doNothing().when(senderEntity).setSenderSettleMode(SenderSettleMode.UNSETTLED);

        doNothing().when(senderViaEntity).setTarget(any(Target.class));
        doNothing().when(senderEntity).setTarget(any(Target.class));
        when(senderEntity.attachments()).thenReturn(record);
        when(senderViaEntity.attachments()).thenReturn(record);

        when(reactorProvider.getReactorDispatcher()).thenReturn(dispatcher);

        Duration openTimeout = Duration.ofSeconds(60);
        serviceBusReactorSession = new ServiceBusReactorSession(session, handler, sessionName, reactorProvider,
            handlerProvider, cbsNodeSupplier, tokenManagerProvider, openTimeout, messageSerializer, retryPolicy);
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        logger.info("[{}] Tearing down.", testInfo.getDisplayName());

        Mockito.framework().clearInlineMocks();
    }

    /**
     * Test for create Sender Link when via-queue is used.
     */
    @Test
    void createViaSenderLink() throws IOException {
        // Arrange
        final Duration timeout = Duration.ofSeconds(20);
        final AmqpRetryPolicy retryMock = mock(AmqpRetryPolicy.class);
        doNothing().when(dispatcher).invoke(any(Runnable.class));

        // Act
        serviceBusReactorSession.createProducer(viaEntityPathSenderLinkName, viaEntityPath, timeout, retryMock, entityPath)
            .subscribe();

        // Assert
        verify(tokenManagerEntity).authorize();
        verify(tokenManagerViaQueue).authorize();

        verify(dispatcher).invoke(dispatcherCaptor.capture());
        List<Runnable> invocations = dispatcherCaptor.getAllValues();

        // Apply the invocation.
        invocations.get(0).run();

        verify(senderViaEntity).setProperties(argThat(linkProperties ->
            ((String) linkProperties.get(LINK_TRANSFER_DESTINATION_PROPERTY)).equalsIgnoreCase(entityPath)
        ));
    }

    /**
     * Test for create Sender Link when via-queue is used but `transferEntityPath` authorization fails.
     */
    @Test
    void createViaSenderLinkDestinationEntityAuthorizeFails() throws IOException {
        // Arrange
        final Duration timeout = Duration.ofSeconds(20);
        final AmqpRetryPolicy retryMock = mock(AmqpRetryPolicy.class);
        final Throwable authorizeError = new RuntimeException("Failed to Authorize EntityPath");
        doNothing().when(dispatcher).invoke(any(Runnable.class));
        when(tokenManagerEntity.authorize()).thenReturn(Mono.error(authorizeError));

        // Act
        StepVerifier.create(serviceBusReactorSession.createProducer(viaEntityPathSenderLinkName, viaEntityPath, timeout,
            retryMock, entityPath)).verifyError(RuntimeException.class);

        // Assert
        verify(tokenManagerEntity).authorize();
        verify(tokenManagerViaQueue).authorize();
        verifyNoInteractions(dispatcher);
    }

    /**
     * Test for create Sender Link.
     */
    @Test
    void createSenderLink() throws IOException {
        // Arrange
        final Duration timeout = Duration.ofSeconds(20);
        final AmqpRetryPolicy retryMock = mock(AmqpRetryPolicy.class);
        doNothing().when(dispatcher).invoke(any(Runnable.class));

        // Act
        serviceBusReactorSession.createProducer(entityPath, entityPath, timeout, retryMock, null)
            .subscribe();

        // Assert
        verify(tokenManagerEntity).authorize();
        verify(tokenManagerViaQueue, never()).authorize();

        verify(dispatcher).invoke(dispatcherCaptor.capture());
        List<Runnable> invocations = dispatcherCaptor.getAllValues();

        // Apply the invocation.
        invocations.get(0).run();

        verify(senderViaEntity, never()).setProperties(anyMap());
    }
}
