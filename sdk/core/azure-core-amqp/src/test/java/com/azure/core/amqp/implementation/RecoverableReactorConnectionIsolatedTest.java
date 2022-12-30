// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.RecoverableReactorConnectionTest.ConnectionState;
import com.azure.core.amqp.implementation.RecoverableReactorConnectionTest.ConnectionSupplier;
import org.apache.qpid.proton.engine.EndpointState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class RecoverableReactorConnectionIsolatedTest {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration VIRTUAL_TIME_SHIFT = OPERATION_TIMEOUT.plusSeconds(30);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(OPERATION_TIMEOUT);
    private final FixedAmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(retryOptions);
    @Mock
    private AmqpErrorContext errorContext;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryIfWaitForConnectionActiveTimeout() {
        final int connectionsCount = 2;
        final Deque<ConnectionState> connectionStates = new ArrayDeque<>(connectionsCount);
        // The first connection with the state never emit so the wait for 'active' timeout.
        connectionStates.add(ConnectionState.never());
        // The second connection with the active state.
        connectionStates.add(ConnectionState.as(EndpointState.ACTIVE));

        final ConnectionSupplier connectionSupplier = new ConnectionSupplier(connectionStates, retryOptions);
        final RecoverableReactorConnection recoverableConnection = new RecoverableReactorConnection(connectionSupplier,
            retryPolicy,
            errorContext,
            new HashMap<>());
        try {
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> recoverableConnection.getConnection())
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .expectNextMatches(con -> {
                        connectionSupplier.assertConnection(con);
                        Assertions.assertFalse(con.isDisposed());
                        return true;
                    })
                    .verifyComplete();
                // Two connection should be supplied (initial connection and second connection on retry
                // when initial connection timeout).
                connectionSupplier.assertInvocationCount(connectionsCount);
            }
        } finally {
            connectionSupplier.dispose();
            recoverableConnection.terminate();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryIfConnectionClosesWithoutBeingActive() {
        final int connectionsCount = 2;
        final Deque<ConnectionState> connectionStates = new ArrayDeque<>(connectionsCount);
        // The first connection with the state that directly completes without being active.
        connectionStates.add(ConnectionState.complete());
        // The second connection with the active state.
        connectionStates.add(ConnectionState.as(EndpointState.ACTIVE));

        final ConnectionSupplier connectionSupplier = new ConnectionSupplier(connectionStates, retryOptions);
        final RecoverableReactorConnection recoverableConnection = new RecoverableReactorConnection(connectionSupplier,
            retryPolicy,
            errorContext,
            new HashMap<>());
        try {
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> recoverableConnection.getConnection())
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .expectNextMatches(con -> {
                        connectionSupplier.assertConnection(con);
                        Assertions.assertFalse(con.isDisposed());
                        return true;
                    })
                    .verifyComplete();
                // Two connection should be supplied (initial connection and second connection on retry
                // when initial connection complete without emitting any state).
                connectionSupplier.assertInvocationCount(connectionsCount);
            }
        } finally {
            connectionSupplier.dispose();
            recoverableConnection.terminate();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryOnRetriableErrors() {
        final int connectionsCount = 4;
        final Deque<ConnectionState> connectionStates = new ArrayDeque<>(connectionsCount);
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable1", null)));
        connectionStates.add(ConnectionState.error(new RejectedExecutionException("retriable2")));
        connectionStates.add(ConnectionState.error(new IllegalStateException("retriable3")));
        connectionStates.add(ConnectionState.as(EndpointState.ACTIVE));

        final ConnectionSupplier connectionSupplier = new ConnectionSupplier(connectionStates, retryOptions);
        final RecoverableReactorConnection recoverableConnection = new RecoverableReactorConnection(connectionSupplier,
            retryPolicy,
            errorContext,
            new HashMap<>());
        try {
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> recoverableConnection.getConnection())
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .expectNextMatches(con -> {
                        connectionSupplier.assertConnection(con);
                        Assertions.assertFalse(con.isDisposed());
                        return true;
                    })
                    .verifyComplete();
                connectionSupplier.assertInvocationCount(connectionsCount);
            }
        } finally {
            connectionSupplier.dispose();
            recoverableConnection.terminate();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void retryShouldNeverExhaustProvidedErrorsAreRetriable() {
        final int connectionsCount = 8;
        final int maxRetryCount = 4;
        final Deque<ConnectionState> connectionStates = new ArrayDeque<>(connectionsCount);
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable0", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable1", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable2", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable3", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable4", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable5", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable6", null)));
        connectionStates.add(ConnectionState.as(EndpointState.ACTIVE));

        final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setTryTimeout(OPERATION_TIMEOUT)
            .setMaxRetries(maxRetryCount);
        final FixedAmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(retryOptions);

        final ConnectionSupplier connectionSupplier = new ConnectionSupplier(connectionStates, retryOptions);
        final RecoverableReactorConnection recoverableConnection = new RecoverableReactorConnection(connectionSupplier,
            retryPolicy,
            errorContext,
            new HashMap<>());
        try {
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> recoverableConnection.getConnection())
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .expectNextMatches(con -> {
                        connectionSupplier.assertConnection(con);
                        Assertions.assertFalse(con.isDisposed());
                        return true;
                    })
                    .verifyComplete();
                // assert that all 8 connections were supplied though the max-retry was 4.
                connectionSupplier.assertInvocationCount(connectionsCount);
            }
        } finally {
            connectionSupplier.dispose();
            recoverableConnection.terminate();
        }
    }

    private static final class VirtualTimeStepVerifier implements AutoCloseable {
        private final VirtualTimeScheduler scheduler;

        VirtualTimeStepVerifier() {
            scheduler = VirtualTimeScheduler.create();
        }

        <T> StepVerifier.Step<T> create(Supplier<Mono<T>> scenarioSupplier) {
            return StepVerifier.withVirtualTime(scenarioSupplier, () -> scheduler, 0);
        }

        @Override
        public void close() {
            scheduler.dispose();
        }
    }
}
