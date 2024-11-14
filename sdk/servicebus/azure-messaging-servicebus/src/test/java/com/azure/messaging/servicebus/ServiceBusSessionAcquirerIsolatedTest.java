// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class ServiceBusSessionAcquirerIsolatedTest {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusSessionAcquirerIsolatedTest.class);
    private static final String IDENTIFIER = "identifier";
    private static final String ENTITY_PATH = "q0";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.QUEUE;
    private static final ServiceBusReceiveMode RECEIVE_MODE = ServiceBusReceiveMode.PEEK_LOCK;
    private static final Duration TRY_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration AWAIT_DURATION = TRY_TIMEOUT.plusSeconds(5);
    private static final AmqpException BROKER_TIMEOUT_ERROR = new AmqpException(true, AmqpErrorCondition.TIMEOUT_ERROR,
        "com.microsoft:timeout", new AmqpErrorContext(ENTITY_PATH));
    private static final TimeoutException CLIENT_TIMEOUT_ERROR = new TimeoutException("client-side-timeout");

    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void setup() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldMapThenPropagateBrokerTimeoutErrorIfRetryDisabled() {
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>(2);
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        final OnCreateSessionLink onCreateSessionLink = new OnCreateSessionLink(sessionLinks);
        final ConnectionCacheWrapper cacheWrapper = createMockConnectionWrapper(onCreateSessionLink);
        final ServiceBusSessionAcquirer sessionAcquirer = createSessionAcquirer(cacheWrapper, true);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(sessionAcquirer::acquire).thenAwait(AWAIT_DURATION).verifyErrorSatisfies(e -> {
                Assertions.assertInstanceOf(TimeoutException.class, e);
                Assertions.assertNotNull(e.getCause());
                final Throwable cause = e.getCause();
                Assertions.assertEquals(BROKER_TIMEOUT_ERROR, cause);
            });
        }
        Assertions.assertEquals(1, onCreateSessionLink.pending()); // Assert that first error itself is propagated.
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldPropagateAnyErrorIfRetryDisabled() {
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>(1);
        final RuntimeException error = new RuntimeException();
        sessionLinks.add(Mono.error(error));
        final OnCreateSessionLink onCreateSessionLink = new OnCreateSessionLink(sessionLinks);
        final ConnectionCacheWrapper cacheWrapper = createMockConnectionWrapper(onCreateSessionLink);
        final ServiceBusSessionAcquirer sessionAcquirer = createSessionAcquirer(cacheWrapper, true);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(sessionAcquirer::acquire).thenAwait(AWAIT_DURATION).verifyErrorSatisfies(e -> {
                Assertions.assertEquals(error, e);
            });
        }
        Assertions.assertEquals(0, onCreateSessionLink.pending());
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryOnBrokerTimeoutErrorIfRetryEnabled() {
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>(2);
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        final RuntimeException error = new RuntimeException();
        sessionLinks.add(Mono.error(error));
        final OnCreateSessionLink onCreateSessionLink = new OnCreateSessionLink(sessionLinks);
        final ConnectionCacheWrapper cacheWrapper = createMockConnectionWrapper(onCreateSessionLink);
        final ServiceBusSessionAcquirer sessionAcquirer = createSessionAcquirer(cacheWrapper, false);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(sessionAcquirer::acquire).thenAwait(AWAIT_DURATION).verifyErrorSatisfies(e -> {
                Assertions.assertEquals(error, e);
            });
        }
        Assertions.assertEquals(0, onCreateSessionLink.pending());
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryOnTimeoutErrorIfRetryEnabled() {
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>(2);
        sessionLinks.add(Mono.error(BROKER_TIMEOUT_ERROR));
        sessionLinks.add(Mono.error(CLIENT_TIMEOUT_ERROR));
        final RuntimeException error = new RuntimeException();
        sessionLinks.add(Mono.error(error));
        final OnCreateSessionLink onCreateSessionLink = new OnCreateSessionLink(sessionLinks);
        final ConnectionCacheWrapper cacheWrapper = createMockConnectionWrapper(onCreateSessionLink);
        final ServiceBusSessionAcquirer sessionAcquirer = createSessionAcquirer(cacheWrapper, false);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(sessionAcquirer::acquire).thenAwait(AWAIT_DURATION).verifyErrorSatisfies(e -> {
                Assertions.assertEquals(error, e);
            });
        }
        Assertions.assertEquals(0, onCreateSessionLink.pending());
    }

    private ConnectionCacheWrapper createMockConnectionWrapper(OnCreateSessionLink onCreateSessionLink) {
        final ServiceBusAmqpConnection connection = mock(ServiceBusAmqpConnection.class);
        when(connection.createReceiveLink(anyString(), anyString(), any(ServiceBusReceiveMode.class), any(),
            any(MessagingEntityType.class), anyString(), any())).thenAnswer(onCreateSessionLink);

        final ConnectionCacheWrapper cacheWrapper = Mockito.mock(ConnectionCacheWrapper.class);
        when(cacheWrapper.isV2()).thenReturn(true);
        when(cacheWrapper.getConnection()).thenReturn(Mono.just(connection));
        return cacheWrapper;
    }

    private ServiceBusSessionAcquirer createSessionAcquirer(ConnectionCacheWrapper cacheWrapper,
        boolean isTimeoutRetryDisabled) {
        return new ServiceBusSessionAcquirer(LOGGER, IDENTIFIER, ENTITY_PATH, ENTITY_TYPE, RECEIVE_MODE, TRY_TIMEOUT,
            isTimeoutRetryDisabled, cacheWrapper);
    }

    private static final class OnCreateSessionLink implements Answer<Mono<ServiceBusReceiveLink>> {
        final Deque<Mono<ServiceBusReceiveLink>> sessionLinks = new ArrayDeque<>();

        OnCreateSessionLink(Deque<Mono<ServiceBusReceiveLink>> sessionLinks) {
            this.sessionLinks.addAll(sessionLinks);
        }

        int pending() {
            return sessionLinks.size();
        }

        @Override
        public Mono<ServiceBusReceiveLink> answer(InvocationOnMock invocation) {
            final Mono<ServiceBusReceiveLink> link = sessionLinks.poll();
            if (link == null) {
                throw new IllegalStateException("unexpected request when there are no more session links.");
            }
            return link;
        }
    }

    private static final class VirtualTimeStepVerifier implements AutoCloseable {
        private final VirtualTimeScheduler scheduler;

        VirtualTimeStepVerifier() {
            scheduler = VirtualTimeScheduler.create();
        }

        <T> StepVerifier.Step<T> create(Supplier<Mono<T>> scenarioSupplier) {
            return StepVerifier.withVirtualTime(scenarioSupplier, () -> scheduler, Integer.MAX_VALUE);
        }

        @Override
        public void close() {
            scheduler.dispose();
        }
    }
}
