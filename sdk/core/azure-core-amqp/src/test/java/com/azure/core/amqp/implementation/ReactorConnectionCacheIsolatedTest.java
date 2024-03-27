// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ReactorConnectionCacheTest.ConnectionState;
import com.azure.core.amqp.implementation.ReactorConnectionCacheTest.ConnectionSupplier;
import org.apache.qpid.proton.engine.EndpointState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tests for {@link ReactorConnectionCache}.
 * <p>
 * See <a href=
 * "https://github.com/Azure/azure-sdk-for-java/wiki/Unit-Testing#stepverifierwithvirtualtime">stepverifierwithvirtualtime</a>
 * for why this test class needs to run in Isolated mode.
 * </p>
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class ReactorConnectionCacheIsolatedTest {
    private static final String FQDN = "contoso-shopping.servicebus.windows.net";
    private static final String ENTITY_PATH = "orders";
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration VIRTUAL_TIME_SHIFT = OPERATION_TIMEOUT.plusSeconds(30);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(OPERATION_TIMEOUT);
    private final FixedAmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(retryOptions);
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
        // The state for the first connection that never emit, so the wait for the connection to 'active' timeout.
        connectionStates.add(ConnectionState.never());
        // The state for the second connection that will be active.
        connectionStates.add(ConnectionState.as(EndpointState.ACTIVE));

        final ConnectionSupplier connectionSupplier = new ConnectionSupplier(connectionStates, retryOptions);
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        try {
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> connectionMono)
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
            connectionCache.dispose();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryIfConnectionClosesWithoutBeingActive() {
        final int connectionsCount = 2;
        final Deque<ConnectionState> connectionStates = new ArrayDeque<>(connectionsCount);
        // The state for the first connection that directly completes without being active.
        connectionStates.add(ConnectionState.complete());
        // The state for the second connection that will be active.
        connectionStates.add(ConnectionState.as(EndpointState.ACTIVE));

        final ConnectionSupplier connectionSupplier = new ConnectionSupplier(connectionStates, retryOptions);
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        try {
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> connectionMono)
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
            connectionCache.dispose();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldRetryOnRetriableErrors() {
        final int connectionsCount = 4;
        final Deque<ConnectionState> connectionStates = new ArrayDeque<>(connectionsCount);
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable0", null)));
        connectionStates.add(ConnectionState.error(new RejectedExecutionException("retriable1")));
        connectionStates.add(ConnectionState.error(new IllegalStateException("retriable2")));
        connectionStates.add(ConnectionState.as(EndpointState.ACTIVE));

        final ConnectionSupplier connectionSupplier = new ConnectionSupplier(connectionStates, retryOptions);
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        try {
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> connectionMono)
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
            connectionCache.dispose();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void retryShouldNeverExhaustProvidedErrorsAreRetriable() {
        final int connectionsCount = 8;
        final int maxRetryCount = 4;
        // State for all eight connections that the connection supplier supplies, 7 of them emit a retriable error.
        // Though max-retry is 4, given these are retriable errors, all those connections are consumed.
        final Deque<ConnectionState> connectionStates = new ArrayDeque<>(connectionsCount);
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable0", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable1", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable2", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable3", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable4", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable5", null)));
        connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable6", null)));
        connectionStates.add(ConnectionState.as(EndpointState.ACTIVE));

        final AmqpRetryOptions retryOptions
            = new AmqpRetryOptions().setTryTimeout(OPERATION_TIMEOUT).setMaxRetries(maxRetryCount);
        final FixedAmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(retryOptions);

        final ConnectionSupplier connectionSupplier = new ConnectionSupplier(connectionStates, retryOptions);
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        try {
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> connectionMono)
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
            connectionCache.dispose();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldResetRetryAttemptsBeforeRefreshingCache() {
        // Note: retry-cycle is defined as a series of retry-attempts until it obtains an active connection.

        final int connectionsCountSet1 = 7;
        final int connectionsCountSet2 = 6;
        final Deque<ConnectionState> connectionStates = new ArrayDeque<>();
        // First set of 6 connections with retriable states and one connection with active state.
        // All must be consumed by the retry-cycle associated with the first request (subscription)
        // for a connection (i.e. the first time cache is updated).
        for (int i = 0; i < connectionsCountSet1 - 1; i++) {
            connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable" + i, null)));
        }
        connectionStates.add(ConnectionState.as(EndpointState.ACTIVE));

        // Then, the Second set of 5 connections with a retriable state and one connection with active state.
        // All must be consumed by the retry-cycle associated with the second request (subscription)
        // for a connection. (i.e. the second time cache is updated).
        for (int i = 0; i < connectionsCountSet2 - 1; i++) {
            connectionStates.add(ConnectionState.error(new AmqpException(true, "retriable" + i, null)));
        }
        connectionStates.add(ConnectionState.as(EndpointState.ACTIVE));

        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(OPERATION_TIMEOUT).setMaxRetries(4);

        // A RetryPolicy to capture "Retry Attempts" (first parameter to 'calculateRetryDelay') to assert.
        final class TestRetryPolicy extends FixedAmqpRetryPolicy {
            private final List<Integer> allRetryAttempts = new ArrayList<>();

            protected TestRetryPolicy(AmqpRetryOptions retryOptions) {
                super(retryOptions);
            }

            @Override
            protected Duration calculateRetryDelay(int retryAttempts, Duration bDelay, Duration bJitter,
                ThreadLocalRandom rnd) {
                allRetryAttempts.add(retryAttempts);
                return super.calculateRetryDelay(retryAttempts, bDelay, bJitter, rnd);
            }

            List<Integer> getAllRetryAttempts() {
                return new ArrayList<>(allRetryAttempts);
            }
        }

        final TestRetryPolicy retryPolicy = new TestRetryPolicy(retryOptions);
        final ConnectionSupplier connectionSupplier = new ConnectionSupplier(connectionStates, retryOptions);
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        try {
            final Mono<ReactorConnection> connectionMono = connectionCache.get();

            // The first connection request where the internal retry-cycle inspects the state of all connections
            // from set1 and emits the 'active' one (i.e., the last one in set1).
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> connectionMono)
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .expectNextMatches(con -> {
                        Assertions.assertFalse(con.isDisposed());
                        return true;
                    })
                    .verifyComplete();
            }

            connectionSupplier.assertInvocationCount(connectionsCountSet1);
            List<Integer> attempts = retryPolicy.getAllRetryAttempts();
            Assertions.assertEquals(connectionsCountSet1 - 1, attempts.size());
            // expectedAttempts1 is the retry-attempt values in the retry-cycle associated with the first connection.
            // the last retry-attempt in the retry-cycle will be 4 since min(attempt, retryOptions.getMaxRetry()) == 4.
            final List<Integer> expectedAttempts1
                = IntStream.of(new int[] { 0, 1, 2, 3, 4, 4 }).boxed().collect(Collectors.toList());
            Assertions.assertIterableEquals(expectedAttempts1, attempts);

            // Close the cached connection by completing connection endpoint,
            // so the next (second) connection request refreshes cache.
            connectionSupplier.completeEndpointState();

            // The second connection request where the internal retry-cycle inspects the state of all connections
            // from set2 and emits the 'active' one (i.e., the last one in set2).
            try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
                verifier.create(() -> connectionMono)
                    .thenRequest(1)
                    .thenAwait(VIRTUAL_TIME_SHIFT)
                    .expectNextMatches(con -> {
                        Assertions.assertFalse(con.isDisposed());
                        return true;
                    })
                    .verifyComplete();
            }

            connectionSupplier.assertInvocationCount(connectionsCountSet1 + connectionsCountSet2);
            attempts = retryPolicy.getAllRetryAttempts();
            Assertions.assertEquals(connectionsCountSet1 + connectionsCountSet2 - 2, attempts.size());
            // Assert that the retry-cycle associated with the second connection request had retry-attempt "reset" to 0.
            final List<Integer> expectedAttempts2
                = IntStream.of(new int[] { 0, 1, 2, 3, 4 }).boxed().collect(Collectors.toList());
            Assertions.assertIterableEquals(expectedAttempts2,
                attempts.subList(connectionsCountSet1 - 1, attempts.size()));
        } finally {
            connectionSupplier.dispose();
            connectionCache.dispose();
        }
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void specShouldRetryOnRetriableErrors() {
        final Deque<Throwable> retriableErrors = new ArrayDeque<>(4);
        retriableErrors.add(new TimeoutException("retriable0"));
        retriableErrors.add(new AmqpException(true, "retriable1", null));
        retriableErrors.add(new RejectedExecutionException("retriable2"));
        retriableErrors.add(new IllegalStateException("retriable3"));

        final Mono<Void> source = Mono.create(sink -> {
            final Throwable error = retriableErrors.poll();
            if (error != null) {
                sink.error(error);
            } else {
                sink.success();
            }
        });
        final Retry retrySpec = retryWhenSpec(retryPolicy);
        final Mono<Void> sourceWithRetry = source.retryWhen(retrySpec);
        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> sourceWithRetry).thenRequest(1).thenAwait(VIRTUAL_TIME_SHIFT).verifyComplete();
        }
        Assertions.assertTrue(retriableErrors.isEmpty());
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void specShouldPropagateNonRetriableErrors() {
        final Deque<Throwable> errors = new ArrayDeque<>(4);
        errors.add(new TimeoutException("retriable0"));
        errors.add(new AmqpException(true, "retriable1", null));
        final Throwable errorExpectedToPropagate = new Throwable("non-retriable");
        errors.add(errorExpectedToPropagate);
        errors.add(new RejectedExecutionException("retriable2"));

        final Mono<Void> source = Mono.create(sink -> {
            final Throwable error = errors.poll();
            if (error != null) {
                sink.error(error);
            } else {
                sink.error(new RuntimeException("unexpected retry."));
            }
        });
        final Retry retrySpec = retryWhenSpec(retryPolicy);
        final Mono<Void> sourceWithRetry = source.retryWhen(retrySpec);
        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> sourceWithRetry)
                .thenRequest(1)
                .thenAwait(VIRTUAL_TIME_SHIFT)
                .verifyErrorMatches(e -> e == errorExpectedToPropagate);
        }
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.poll() instanceof RejectedExecutionException);
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void specShouldNeverExhaustRetryProvidedErrorsAreRetriable() {
        final int retriableErrorCount = 8;
        final int maxRetryCount = 4;
        // There are 7 retriable error, though max-retry is 4, given these are retriable errors, all those errors will
        // be retried.
        final Deque<Throwable> retriableErrors = new ArrayDeque<>(retriableErrorCount);
        retriableErrors.add(new AmqpException(true, "retriable0", null));
        retriableErrors.add(new AmqpException(true, "retriable1", null));
        retriableErrors.add(new AmqpException(true, "retriable2", null));
        retriableErrors.add(new AmqpException(true, "retriable3", null));
        retriableErrors.add(new AmqpException(true, "retriable4", null));
        retriableErrors.add(new AmqpException(true, "retriable5", null));
        retriableErrors.add(new AmqpException(true, "retriable6", null));

        final Mono<Void> source = Mono.create(sink -> {
            final Throwable error = retriableErrors.poll();
            if (error != null) {
                sink.error(error);
            } else {
                sink.success();
            }
        });
        final AmqpRetryOptions retryOptions
            = new AmqpRetryOptions().setTryTimeout(OPERATION_TIMEOUT).setMaxRetries(maxRetryCount);
        final FixedAmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(retryOptions);
        final Retry retrySpec = retryWhenSpec(retryPolicy);
        final Mono<Void> sourceWithRetry = source.retryWhen(retrySpec);
        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> sourceWithRetry).thenRequest(1).thenAwait(VIRTUAL_TIME_SHIFT).verifyComplete();
        }
        // assert that all 7 errors were consumed though the max-retry was 4.
        Assertions.assertTrue(retriableErrors.isEmpty());
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void specShouldNotRetryAfterCacheDisposal() {
        final Deque<Throwable> retriableErrors = new ArrayDeque<>(1);
        retriableErrors.add(new AmqpException(true, "retriable0", null));

        final Mono<Void> source = Mono.create(sink -> {
            final Throwable error = retriableErrors.poll();
            if (error != null) {
                sink.error(error);
            } else {
                sink.error(new RuntimeException("unexpected retry."));
            }
        });
        final Supplier<ReactorConnection> nopConnectionSupplier = () -> null;
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(nopConnectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        final Retry retrySpec = connectionCache.retryWhenSpec(retryPolicy);
        final Mono<Void> sourceWithRetry = source.retryWhen(retrySpec);
        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            connectionCache.dispose();
            verifier.create(() -> sourceWithRetry)
                .thenRequest(1)
                .thenAwait(VIRTUAL_TIME_SHIFT)
                .verifyErrorSatisfies(e -> {
                    Assertions.assertTrue(e instanceof AmqpException);
                    final AmqpException amqpException = (AmqpException) e;
                    Assertions.assertFalse(amqpException.isTransient());
                    Assertions.assertNotNull(amqpException.getMessage());
                    Assertions.assertTrue(
                        amqpException.getMessage().startsWith("Connection recovery support is terminated."));
                });
        }
    }

    private Retry retryWhenSpec(AmqpRetryPolicy retryPolicy) {
        final Supplier<ReactorConnection> nopConnectionSupplier = () -> null;
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(nopConnectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        return connectionCache.retryWhenSpec(retryPolicy);
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
