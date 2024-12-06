// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.ReceiveLinkHandlerWrapper;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static com.azure.messaging.servicebus.implementation.ServiceBusReactorSession.LOCKED_UNTIL_UTC;
import static com.azure.messaging.servicebus.implementation.ServiceBusReactorSession.SESSION_FILTER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ServiceBusReactorReceiver}
 */
class ServiceBusReactorReceiverTest {
    private static final String ENTITY_PATH = "queue-name";
    private static final String LINK_NAME = "a-link-name";
    private static final String CONNECTION_ID = "a-connection-id";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);

    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReactorReceiver.class);
    private final Sinks.Many<EndpointState> endpointStates
        = Sinks.many().replay().latestOrDefault(EndpointState.UNINITIALIZED);

    private final Sinks.Many<Delivery> deliveries = Sinks.many().multicast().onBackpressureBuffer();

    @Mock
    private Receiver receiver;
    @Mock
    private TokenManager tokenManager;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private ReactorDispatcher reactorDispatcher;
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
    private final AmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(retryOptions);
    @Mock
    private AmqpConnection connection;

    private ServiceBusReactorReceiver reactorReceiver;
    private AutoCloseable openMocks;

    @BeforeEach
    void setup(TestInfo testInfo) throws IOException {
        LOGGER.info("[{}] Setting up.", testInfo.getDisplayName());

        openMocks = MockitoAnnotations.openMocks(this);

        doAnswer(invocation -> {
            LOGGER.info("Running work on dispatcher.");
            return null;
        }).when(reactorDispatcher).invoke(any());

        doAnswer(invocation -> {
            LOGGER.info("Running work on dispatcher.");
            return null;
        }).when(reactorDispatcher).invoke(any(), any());

        ReceiveLinkHandler receiveLinkHandler = new ReceiveLinkHandler(CONNECTION_ID, "", LINK_NAME, "", null) {
            @Override
            public Flux<Delivery> getDeliveredMessages() {
                return deliveries.asFlux();
            }

            @Override
            public Flux<EndpointState> getEndpointStates() {
                return endpointStates.asFlux();
            }
        };

        when(tokenManager.getAuthorizationResults()).thenReturn(Flux.create(sink -> sink.next(AmqpResponseCode.OK)));

        when(connection.getShutdownSignals()).thenReturn(Flux.never());

        reactorReceiver = new ServiceBusReactorReceiver(connection, ENTITY_PATH, receiver,
            new ReceiveLinkHandlerWrapper(receiveLinkHandler), tokenManager, reactorDispatcher, retryOptions);
    }

    @AfterEach
    void teardown(TestInfo testInfo) throws Exception {
        LOGGER.info("[{}] Tearing down.", testInfo.getDisplayName());

        openMocks.close();
        Mockito.framework().clearInlineMock(this);
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
        StepVerifier.create(reactorReceiver.getSessionId())
            .then(() -> endpointStates.emitNext(EndpointState.ACTIVE, Sinks.EmitFailureHandler.FAIL_FAST))
            .expectNext(actualSession)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
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
        StepVerifier.create(reactorReceiver.getSessionId())
            .then(() -> endpointStates.emitNext(EndpointState.ACTIVE, Sinks.EmitFailureHandler.FAIL_FAST))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    /**
     * Gets locked until for sessioned receiver.
     */
    @Test
    void getSessionLockedUntil() {
        // Arrange
        // 2020-04-28 06:42:27
        final long ticks = 637236529470000000L;
        final OffsetDateTime lockedUntil = Instant.ofEpochSecond(1588056147L).atOffset(ZoneOffset.UTC);
        final String actualSession = "a-session-id-from-service";
        final Map<Symbol, Object> properties = new HashMap<>();
        properties.put(SESSION_FILTER, actualSession);
        properties.put(LOCKED_UNTIL_UTC, ticks);

        when(receiver.getRemoteProperties()).thenReturn(properties);

        // Act & Assert
        StepVerifier.create(reactorReceiver.getSessionLockedUntil())
            .then(() -> endpointStates.emitNext(EndpointState.ACTIVE, Sinks.EmitFailureHandler.FAIL_FAST))
            .expectNext(lockedUntil)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }
}
