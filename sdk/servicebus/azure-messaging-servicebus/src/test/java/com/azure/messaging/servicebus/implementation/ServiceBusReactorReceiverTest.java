// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
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
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.azure.messaging.servicebus.implementation.ServiceBusReactorSession.LOCKED_UNTIL_UTC;
import static com.azure.messaging.servicebus.implementation.ServiceBusReactorSession.SESSION_FILTER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ServiceBusReactorReceiver}
 */
class ServiceBusReactorReceiverTest {
    private static final String SESSION_ID = "a-session-id";
    private static final String ENTITY_PATH = "queue-name";
    private static final String LINK_NAME = "a-link-name";

    private final ClientLogger logger = new ClientLogger(ServiceBusReactorReceiver.class);
    private final DirectProcessor<EndpointState> endpointStates = DirectProcessor.create();
    private final FluxSink<EndpointState> endpointStatesSink = endpointStates.sink();

    private final DirectProcessor<Delivery> deliveryProcessor = DirectProcessor.create();
    private final FluxSink<Delivery> deliverySink = deliveryProcessor.sink();

    @Mock
    private Receiver receiver;
    @Mock
    private TokenManager tokenManager;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private ReactorDispatcher reactorDispatcher;
    @Mock
    private AmqpRetryPolicy retryPolicy;
    @Mock
    private ReceiveLinkHandler receiveLinkHandler;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private ServiceBusReactorReceiver reactorReceiver;
    private ServiceBusReactorReceiver sessionReactor;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(100));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        logger.info("[{}] Setting up.", testInfo.getDisplayName());

        MockitoAnnotations.initMocks(this);

        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);

        when(receiveLinkHandler.getDeliveredMessages()).thenReturn(deliveryProcessor);
        when(receiveLinkHandler.getLinkName()).thenReturn(LINK_NAME);
        when(receiveLinkHandler.getEndpointStates()).thenReturn(endpointStates);
        when(receiveLinkHandler.getErrors()).thenReturn(Flux.never());

        when(tokenManager.getAuthorizationResults()).thenReturn(Flux.create(sink -> sink.next(AmqpResponseCode.OK)));

        reactorReceiver = new ServiceBusReactorReceiver(ENTITY_PATH, receiver, receiveLinkHandler, tokenManager,
            reactorProvider, Duration.ofSeconds(20), retryPolicy);

        sessionReactor = new ServiceBusReactorReceiver(ENTITY_PATH, receiver, receiveLinkHandler, tokenManager,
            reactorProvider, Duration.ofSeconds(20), retryPolicy, SESSION_ID);
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        logger.info("[{}] Tearing down.", testInfo.getDisplayName());

        reactorReceiver.dispose();
        sessionReactor.dispose();

        Mockito.framework().clearInlineMocks();
    }

    /**
     * Gets the session id for a sessionful receiver.
     */
    @Test
    void getsSessionId() {
        // Arrange
        final String actualSession = "a-session-id-from-service";
        final Map<Symbol, Object> properties = new HashMap<>();
        properties.put(SESSION_FILTER, actualSession);

        final Source remoteSource = mock(Source.class);
        when(receiver.getRemoteSource()).thenReturn(remoteSource);
        when(remoteSource.getFilter()).thenReturn(properties);

        // Act & Assert
        StepVerifier.create(sessionReactor.getSessionId())
            .then(() -> endpointStatesSink.next(EndpointState.ACTIVE))
            .expectNext(actualSession)
            .verifyComplete();
    }

    /**
     * A non session receive link does not have a session id.
     */
    @Test
    void nonSessionReceiverNoSessionId() {
        // Arrange
        final String actualSession = "a-session-id-from-service";
        final Map<Symbol, Object> properties = new HashMap<>();
        properties.put(SESSION_FILTER, actualSession);

        final Source remoteSource = mock(Source.class);
        when(receiver.getRemoteSource()).thenReturn(remoteSource);
        when(remoteSource.getFilter()).thenReturn(properties);

        // Act & Assert
        StepVerifier.create(reactorReceiver.getSessionId())
            .then(() -> endpointStatesSink.next(EndpointState.ACTIVE))
            .verifyComplete();
    }

    /**
     * A non session receive link does not have a session id.
     */
    @Test
    void sessionReceiverNoSessionId() {
        // Arrange
        final Map<Symbol, Object> properties = new HashMap<>();

        final Source remoteSource = mock(Source.class);
        when(receiver.getRemoteSource()).thenReturn(remoteSource);
        when(remoteSource.getFilter()).thenReturn(properties);

        // Act & Assert
        StepVerifier.create(sessionReactor.getSessionId())
            .then(() -> endpointStatesSink.next(EndpointState.ACTIVE))
            .verifyComplete();
    }

    /**
     * Gets locked until for sessioned receiver.
     */
    @Test
    void getSessionLockedUntil() {
        // Arrange
        // 2020-04-28 06:42:27
        final long ticks = 637236529470000000L;
        final Instant lockedUntil = Instant.ofEpochSecond(1588056147L);
        final String actualSession = "a-session-id-from-service";
        final Map<Symbol, Object> properties = new HashMap<>();
        properties.put(SESSION_FILTER, actualSession);
        properties.put(LOCKED_UNTIL_UTC, ticks);

        when(receiver.getRemoteProperties()).thenReturn(properties);

        // Act & Assert
        StepVerifier.create(sessionReactor.getSessionLockedUntil())
            .then(() -> endpointStatesSink.next(EndpointState.ACTIVE))
            .expectNext(lockedUntil)
            .verifyComplete();
    }

    /**
     * Gets locked until for sessioned receiver.
     */
    @Test
    void nonSessionGetLockedUntil() {
        // Arrange
        // 2020-04-28 06:42:27
        final long ticks = 637236529470000000L;
        final Map<Symbol, Object> properties = new HashMap<>();
        properties.put(LOCKED_UNTIL_UTC, ticks);
        final Instant epoch = Instant.EPOCH;

        when(receiver.getRemoteProperties()).thenReturn(properties);

        // Act & Assert
        StepVerifier.create(reactorReceiver.getSessionLockedUntil())
            .then(() -> endpointStatesSink.next(EndpointState.ACTIVE))
            .expectNext(epoch)
            .verifyComplete();
    }
}
