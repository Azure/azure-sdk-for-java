// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Deque;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReactorConnectionCacheTest {
    private static final String FQDN = "contoso-shopping.servicebus.windows.net";
    private static final String ENTITY_PATH = "orders";
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(30);
    @Mock
    private AmqpRetryPolicy retryPolicy;
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
    public void shouldGetConnection() {
        final ConnectionSupplier connectionSupplier = new ConnectionSupplier();
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        try {
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            // The request (subscription) for connection should get a connection.
            StepVerifier.create(connectionMono, 0)
                .thenRequest(1)
                .then(() -> connectionSupplier.emitEndpointState(EndpointState.ACTIVE))
                .expectNextMatches(con -> {
                    connectionSupplier.assertConnection(con);
                    Assertions.assertFalse(con.isDisposed());
                    return true;
                })
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
            // Assert that there was only one connection supplied.
            connectionSupplier.assertInvocationCount(1);
        } finally {
            connectionSupplier.dispose();
            connectionCache.dispose();
        }
    }

    @Test
    public void shouldCacheConnection() {
        final ConnectionSupplier connectionSupplier = new ConnectionSupplier();
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        try {
            final ReactorConnection[] c = new ReactorConnection[1];
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            // The first request (subscription) for connection populates the cache.
            StepVerifier.create(connectionMono, 0)
                .thenRequest(1)
                .then(() -> connectionSupplier.emitEndpointState(EndpointState.ACTIVE))
                .expectNextMatches(con -> {
                    connectionSupplier.assertConnection(con);
                    Assertions.assertFalse(con.isDisposed());
                    // Store the connection to assert that the same is returned for later connection request.
                    c[0] = con;
                    return true;
                })
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
            connectionSupplier.assertInvocationCount(1);

            // Later a second connection request (Subscription) must be served from the cache.
            StepVerifier.create(connectionMono, 0).thenRequest(1).expectNextMatches(con -> {
                // Assert the second subscription got the same connection (cached) as first subscription.
                Assertions.assertEquals(c[0], con);
                return true;
            }).expectComplete().verify(VERIFY_TIMEOUT);

            // Assert that there was only one connection supplied.
            connectionSupplier.assertInvocationCount(1);
        } finally {
            connectionSupplier.dispose();
            connectionCache.dispose();
        }
    }

    @Test
    public void shouldRefreshCacheOnCompletionOfCachedConnection() {
        final ConnectionSupplier connectionSupplier = new ConnectionSupplier();
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        try {
            final ReactorConnection[] c = new ReactorConnection[1];
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            // The first request (subscription) for connection populates the cache.
            StepVerifier.create(connectionMono, 0)
                .thenRequest(1)
                .then(() -> connectionSupplier.emitEndpointState(EndpointState.ACTIVE))
                .expectNextMatches(con -> {
                    connectionSupplier.assertConnection(con);
                    Assertions.assertFalse(con.isDisposed());
                    // Store the connection to assert that once it's closed later connection request
                    // gets a (new) different connection.
                    c[0] = con;
                    return true;
                })
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
            connectionSupplier.assertInvocationCount(1);

            // Close the cached connection by completing connection endpoint.
            connectionSupplier.completeEndpointState();

            // A new request (subscription) for connection should refresh cache.
            StepVerifier.create(connectionMono, 0)
                .thenRequest(1)
                .then(() -> connectionSupplier.emitEndpointState(EndpointState.ACTIVE))
                .expectNextMatches(con -> {
                    Assertions.assertFalse(con.isDisposed());
                    // Assert the second subscription got a new connection as a result of cache refresh.
                    Assertions.assertNotEquals(c[0], con);
                    return true;
                })
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            // Assert that total two connections were supplied.
            connectionSupplier.assertInvocationCount(2);
        } finally {
            connectionSupplier.dispose();
            connectionCache.dispose();
        }
    }

    @Test
    public void shouldRefreshCacheOnErrorInCachedConnection() {
        final ConnectionSupplier connectionSupplier = new ConnectionSupplier();
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        try {
            final ReactorConnection[] c = new ReactorConnection[1];
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            // The first request (subscription) for connection populates the cache.
            StepVerifier.create(connectionMono, 0)
                .thenRequest(1)
                .then(() -> connectionSupplier.emitEndpointState(EndpointState.ACTIVE))
                .expectNextMatches(con -> {
                    connectionSupplier.assertConnection(con);
                    Assertions.assertFalse(con.isDisposed());
                    // Store the connection to assert that once it's closed, later connection request
                    // gets a (new) different connection.
                    c[0] = con;
                    return true;
                })
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
            connectionSupplier.assertInvocationCount(1);

            // Close the cached connection by error-ing connection endpoint.
            connectionSupplier.errorEndpointState(new RuntimeException("connection dropped"));

            // A new request (subscription) for connection should refresh cache.
            StepVerifier.create(connectionMono, 0)
                .thenRequest(1)
                .then(() -> connectionSupplier.emitEndpointState(EndpointState.ACTIVE))
                .expectNextMatches(con -> {
                    connectionSupplier.assertConnection(con);
                    Assertions.assertFalse(con.isDisposed());
                    // Assert the second subscription got a new connection as a result of cache refresh.
                    Assertions.assertNotEquals(c[0], con);
                    return true;
                })
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            // Assert that total two connections were supplied.
            connectionSupplier.assertInvocationCount(2);
        } finally {
            connectionSupplier.dispose();
            connectionCache.dispose();
        }
    }

    @Test
    public void shouldBubbleUpNonRetriableError() {
        final ConnectionSupplier connectionSupplier = new ConnectionSupplier();
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        final Throwable nonRetriableError = new Throwable("non-retriable");
        try {
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            // The first request (subscription) fails with non-retriable error.
            StepVerifier.create(connectionMono, 0)
                .thenRequest(1)
                .then(() -> connectionSupplier.errorEndpointState(nonRetriableError))
                .expectErrorSatisfies(e -> {
                    Assertions.assertEquals(nonRetriableError, e);
                })
                .verify(VERIFY_TIMEOUT);
            connectionSupplier.assertInvocationCount(1);

            // A new request (subscription) for connection should obtain a connection refreshing cache.
            StepVerifier.create(connectionMono, 0)
                .thenRequest(1)
                .then(() -> connectionSupplier.emitEndpointState(EndpointState.ACTIVE))
                .expectNextMatches(con -> {
                    Assertions.assertFalse(con.isDisposed());
                    connectionSupplier.assertConnection(con);
                    return true;
                })
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            // Assert that total two connections were supplied.
            connectionSupplier.assertInvocationCount(2);
        } finally {
            connectionSupplier.dispose();
            connectionCache.dispose();
        }
    }

    @Test
    public void shouldDisposeConnectionUponTermination() {
        final ConnectionSupplier connectionSupplier = new ConnectionSupplier();
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        final ReactorConnection[] c = new ReactorConnection[1];
        try {
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            StepVerifier.create(connectionMono, 0)
                .thenRequest(1)
                .then(() -> connectionSupplier.emitEndpointState(EndpointState.ACTIVE))
                .expectNextMatches(con -> {
                    connectionSupplier.assertConnection(con);
                    Assertions.assertFalse(con.isDisposed());
                    c[0] = con;
                    return true;
                })
                .expectComplete()
                .verify(VERIFY_TIMEOUT);
            connectionSupplier.assertInvocationCount(1);
            Assertions.assertFalse(c[0].isDisposed());
        } finally {
            // Connection support is terminated, and such a termination...
            connectionCache.dispose();
        }

        try {
            // ...should dispose current connection.
            Assertions.assertTrue(c[0].isDisposed());
        } finally {
            connectionSupplier.dispose();
        }
    }

    @Test
    public void shouldNotProvideConnectionAfterTermination() {
        final ConnectionSupplier connectionSupplier = new ConnectionSupplier();
        final ReactorConnectionCache<ReactorConnection> connectionCache
            = new ReactorConnectionCache<>(connectionSupplier, FQDN, ENTITY_PATH, retryPolicy, new HashMap<>());
        // Terminating the recovery support.
        connectionCache.dispose();
        try {
            final Mono<ReactorConnection> connectionMono = connectionCache.get();
            // Attempt to obtain a connection post the termination of recovery support will fail.
            StepVerifier.create(connectionMono, 0).thenRequest(1).expectErrorSatisfies(e -> {
                Assertions.assertTrue(e instanceof AmqpException);
                final AmqpException amqpException = (AmqpException) e;
                Assertions.assertFalse(amqpException.isTransient());
                Assertions.assertEquals("Connection recovery support is terminated.", amqpException.getMessage());
            }).verify(VERIFY_TIMEOUT);
            connectionSupplier.assertInvocationCount(0);
        } finally {
            connectionSupplier.dispose();
        }
    }

    static final class ConnectionSupplier implements Supplier<ReactorConnection> {
        private final AmqpRetryOptions retryOptions;
        // Describes the endpoint state (EndpointState.*, error, completion) of each connection that
        // the supplier returns upon invocation. Supplier invocation after emptying queue throws.
        // If the queue is null, then each invocation returns a new connection with no endpoint state set yet.
        private final Deque<ConnectionState> connectionsStateQueue;
        private int invocationCount;
        private Sinks.Many<EndpointState> currentConnectionStates;
        private ReactorConnection currentConnection;

        ConnectionSupplier() {
            // the queue is null, so each invocation of 'get' returns a new connection with no endpoint state set yet.
            // Depending on the use case being tested, the test case can set the state later by calling
            // 'emitEndpointState(EndpointState)', 'completeEndpointState()' or 'errorEndpointState(Throwable)'.
            this.connectionsStateQueue = null;
            this.retryOptions = new AmqpRetryOptions();
        }

        ConnectionSupplier(Deque<ConnectionState> connectionsStateQueue, AmqpRetryOptions retryOptions) {
            Objects.requireNonNull(connectionsStateQueue);
            // the queue is set, so each invocation of 'get' returns a new connection with the state dequeued from
            // the queue. Invoking 'get' for connection once the queue is empty will throw.
            this.connectionsStateQueue = connectionsStateQueue;
            this.retryOptions = retryOptions;
        }

        // implements Supplier<ReactorConnection>::get
        @Override
        public ReactorConnection get() {
            if (currentConnection != null && !currentConnection.isDisposed()) {
                throw new RuntimeException("Unexpected request for new connection when current one is not disposed.");
            }

            invocationCount++;
            currentConnectionStates = Sinks.many().replay().latestOrDefault(EndpointState.UNINITIALIZED);
            currentConnection = createMockConnection(String.valueOf(invocationCount),
                currentConnectionStates.asFlux().distinctUntilChanged());
            if (connectionsStateQueue != null) {
                final ConnectionState connectionState = connectionsStateQueue.remove();
                connectionState.apply(currentConnectionStates);
            }
            return currentConnection;
        }

        void assertConnection(ReactorConnection con) {
            Assertions.assertNotNull(currentConnection);
            Assertions.assertEquals(currentConnection, con);
        }

        void assertInvocationCount(int c) {
            Assertions.assertEquals(invocationCount, c);
        }

        void emitEndpointState(EndpointState state) {
            Objects.requireNonNull(currentConnectionStates);
            currentConnectionStates.emitNext(state, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void completeEndpointState() {
            Objects.requireNonNull(currentConnectionStates);
            currentConnectionStates.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void errorEndpointState(Throwable error) {
            Objects.requireNonNull(currentConnectionStates);
            currentConnectionStates.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void dispose() {
            if (currentConnection != null) {
                currentConnection.closeAsync().block();
            }
        }

        private ReactorConnection createMockConnection(String id, Flux<EndpointState> endpointStates) {
            final ConnectionOptions connectionOptions = mock(ConnectionOptions.class);
            when(connectionOptions.getRetry()).thenReturn(retryOptions);

            final Connection connectionInner = mock(Connection.class);
            doNothing().when(connectionInner).close();

            final Reactor reactor = mock(Reactor.class);
            when(reactor.connectionToHost(any(), anyInt(), any())).thenReturn(connectionInner);

            final ReactorDispatcher reactorDispatcher = mock(ReactorDispatcher.class);
            try {
                doAnswer(invocation -> {
                    final Runnable work = invocation.getArgument(0);
                    work.run();
                    return null;
                }).when(reactorDispatcher).invoke(any(Runnable.class));
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
            when(reactorDispatcher.getShutdownSignal()).thenReturn(Mono.empty());

            final ReactorExecutor reactorExecutor = mock(ReactorExecutor.class);
            doNothing().when(reactorExecutor).start();
            when(reactorExecutor.closeAsync()).thenReturn(Mono.empty());

            final ReactorProvider reactorProvider = mock(ReactorProvider.class);
            try {
                when(reactorProvider.createReactor(anyString(), anyInt())).thenReturn(reactor);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
            when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
            when(reactorProvider.createExecutor(any(), anyString(), any(), any(), any())).thenReturn(reactorExecutor);

            final ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
            when(connectionHandler.getEndpointStates()).thenReturn(endpointStates);
            doNothing().when(connectionHandler).close();
            final ReactorHandlerProvider handlerProvider = mock(ReactorHandlerProvider.class);
            when(handlerProvider.createConnectionHandler(anyString(), any())).thenReturn(connectionHandler);

            final ReactorConnection connection = new ReactorConnection(id, connectionOptions, reactorProvider,
                handlerProvider, mock(AmqpLinkProvider.class), mock(TokenManagerProvider.class),
                mock(MessageSerializer.class), SenderSettleMode.SETTLED, ReceiverSettleMode.FIRST, true);
            return connection;
        }
    }

    // Describes the state of a connection that 'ConnectionSupplier' utility class provides.
    static final class ConnectionState {
        private final boolean never;
        private final boolean complete;
        private final Throwable error;
        private final EndpointState state;

        private ConnectionState(boolean never, boolean complete, Throwable error, EndpointState state) {
            this.never = never;
            this.complete = complete;
            this.error = error;
            this.state = state;
        }

        static ConnectionState never() {
            return new ConnectionState(true, false, null, null);
        }

        static ConnectionState complete() {
            return new ConnectionState(false, true, null, null);
        }

        static ConnectionState error(Throwable error) {
            Objects.requireNonNull(error);
            return new ConnectionState(false, false, error, null);
        }

        static ConnectionState as(EndpointState state) {
            Objects.requireNonNull(state);
            return new ConnectionState(false, false, null, state);
        }

        // Apply the state to the given sink.
        void apply(Sinks.Many<EndpointState> sink) {
            if (complete) {
                sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            } else if (error != null) {
                sink.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
            } else if (state != null) {
                sink.emitNext(state, Sinks.EmitFailureHandler.FAIL_FAST);
            }
            // else if (never) {
            // NOP (the sink never emits).
            // }
        }
    }
}
