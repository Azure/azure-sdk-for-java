// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReactorSessionCacheTest {
    private static final String CONNECTION_ID = "contoso-connection-id";
    private static final String NAMESPACE = "contoso.servicebus.windows.net";
    private static final Duration OPEN_TIMEOUT = Duration.ZERO;
    private final ReactorProvider reactorProvider = new ReactorProvider();
    private final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(reactorProvider, null);
    private final ClientLogger logger = new ClientLogger(ReactorSessionCacheTest.class);

    private AutoCloseable mocksCloseable;

    @BeforeEach
    void session() {
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
    void shouldCacheSession() {
        final ReactorSessionCache cache = createSessionCache();
        final Connection connection = mock(Connection.class);

        final String session0Name = "session-0";
        final ReactorSession session0 = session(session0Name, endpointStatesSink(), null);

        final HashMap<String, Deque<ReactorSession>> cacheLoaderLookup = new HashMap<>();
        cacheLoaderLookup.put(session0Name, sessions(session0));
        final CacheLoader cacheLoader = new CacheLoader(cacheLoaderLookup);

        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0)
            .verifyComplete();

        // Since the session0 is never terminated (i.e., it's endpointStates never errors or completes), the below
        // cache lookup should get the same session0.
        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0)
            .verifyComplete();
    }

    @Test
    void shouldAutoEvictCompletedSession() {
        final ReactorSessionCache cache = createSessionCache();
        final Connection connection = mock(Connection.class);

        final String session0Name = "session-0";
        final Sinks.Many<AmqpEndpointState> session0aEndpointStates = endpointStatesSink();
        final ReactorSession session0a = session(session0Name, session0aEndpointStates, null);
        final ReactorSession session0b = session(session0Name, endpointStatesSink(), null);

        final HashMap<String, Deque<ReactorSession>> cacheLoaderLookup = new HashMap<>();
        cacheLoaderLookup.put(session0Name, sessions(session0a, session0b));
        final CacheLoader cacheLoader = new CacheLoader(cacheLoaderLookup);

        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0a)
            .verifyComplete();

        // Signal that session0a is terminated by completion.
        session0aEndpointStates.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);

        // Since the session0a is completed, the cache should return the new session, session0b.
        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0b)
            .verifyComplete();
    }

    @Test
    void shouldAutoEvictErroredSession() {
        final ReactorSessionCache cache = createSessionCache();
        final Connection connection = mock(Connection.class);

        final String session0Name = "session-0";
        final Sinks.Many<AmqpEndpointState> session0aEndpointStates = endpointStatesSink();
        final ReactorSession session0a = session(session0Name, session0aEndpointStates, null);
        final ReactorSession session0b = session(session0Name, endpointStatesSink(), null);

        final HashMap<String, Deque<ReactorSession>> cacheLoaderLookup = new HashMap<>();
        cacheLoaderLookup.put(session0Name, sessions(session0a, session0b));
        final CacheLoader cacheLoader = new CacheLoader(cacheLoaderLookup);

        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0a)
            .verifyComplete();

        // Signal that session0a is terminated by error.
        session0aEndpointStates.emitError(new RuntimeException("session detached"), Sinks.EmitFailureHandler.FAIL_FAST);

        // Since the session0a is errored, the cache should return the new session, session0b.
        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0b)
            .verifyComplete();
    }

    @Test
    void shouldEvictOnSessionOpenError() {
        final ReactorSessionCache cache = createSessionCache();
        final Connection connection = mock(Connection.class);

        final String session0Name = "session-0";
        final Sinks.Many<AmqpEndpointState> session0aEndpointStates = endpointStatesSink();
        final Throwable session0aOpenError = new RuntimeException("session0a open failed");
        final ReactorSession session0a = session(session0Name, session0aEndpointStates, session0aOpenError);
        final ReactorSession session0b = session(session0Name, endpointStatesSink(), null);

        final HashMap<String, Deque<ReactorSession>> cacheLoaderLookup = new HashMap<>();
        cacheLoaderLookup.put(session0Name, sessions(session0a, session0b));
        final CacheLoader cacheLoader = new CacheLoader(cacheLoaderLookup);

        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .verifyErrorMatches(e -> e == session0aOpenError);

        // Since the session0a open attempt is errored, the cache should return the new session, session0b.
        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0b)
            .verifyComplete();
    }

    @Test
    void shouldExplicitEvictRemoveSession() {
        final ReactorSessionCache cache = createSessionCache();
        final Connection connection = mock(Connection.class);

        final String session0Name = "session-0";
        final Sinks.Many<AmqpEndpointState> session0aEndpointStates = endpointStatesSink();
        final ReactorSession session0a = session(session0Name, session0aEndpointStates, null);
        final ReactorSession session0b = session(session0Name, endpointStatesSink(), null);

        final HashMap<String, Deque<ReactorSession>> cacheLoaderLookup = new HashMap<>();
        cacheLoaderLookup.put(session0Name, sessions(session0a, session0b));
        final CacheLoader cacheLoader = new CacheLoader(cacheLoaderLookup);

        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0a)
            .verifyComplete();

        // explicitly evict session0a.
        Assertions.assertTrue(cache.evict(session0Name));

        // Since the session0a was evicted, the cache should return the new session, session0b.
        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0b)
            .verifyComplete();
    }

    @Test
    void shouldNotEvictSessionIfOwnerDisposed() {
        final ReactorSessionCache cache = createSessionCache();
        final Connection connection = mock(Connection.class);

        final String session0Name = "session-0";
        final Sinks.Many<AmqpEndpointState> session0EndpointStates = endpointStatesSink();
        final ReactorSession session0 = session(session0Name, endpointStatesSink(), null);

        final HashMap<String, Deque<ReactorSession>> cacheLoaderLookup = new HashMap<>();
        cacheLoaderLookup.put(session0Name, sessions(session0));
        final CacheLoader cacheLoader = new CacheLoader(cacheLoaderLookup);

        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0)
            .verifyComplete();

        // Signal that the cache owner (Connection) is disposed.
        cache.setOwnerDisposed();
        // Signal that session0a is terminated after owner disposal.
        session0EndpointStates.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);

        // Since the owner is disposed, the cache should not evict the session0 even if it's terminated.
        StepVerifier.create(cache.getOrLoad(Mono.just(connection), session0Name, cacheLoader))
            .expectNext(session0)
            .verifyComplete();
    }

    private ReactorSessionCache createSessionCache() {
        return new ReactorSessionCache(CONNECTION_ID, NAMESPACE, handlerProvider, reactorProvider, OPEN_TIMEOUT,
            logger);
    }

    private static Sinks.Many<AmqpEndpointState> endpointStatesSink() {
        return Sinks.many().replay().latestOrDefault(AmqpEndpointState.UNINITIALIZED);
    }

    private static Deque<ReactorSession> sessions(ReactorSession... sessions) {
        final Deque<ReactorSession> queue = new ArrayDeque<>(sessions.length);
        Collections.addAll(queue, sessions);
        return queue;
    }

    private static ReactorSession session(String sessionName, Sinks.Many<AmqpEndpointState> sink, Throwable openError) {
        final ReactorSession session = mock(ReactorSession.class);
        when(session.getSessionName()).thenReturn(sessionName);
        when(session.getEndpointStates()).thenReturn(sink.asFlux());
        if (openError != null) {
            when(session.open()).thenReturn(Mono.error(openError));
        } else {
            when(session.open()).thenReturn(Mono.just(session));
        }
        when(session.closeAsync(anyString(), any(), eq(true))).thenReturn(Mono.empty());
        return session;
    }

    private static final class CacheLoader implements ReactorSessionCache.Loader {
        private final HashMap<String, Deque<ReactorSession>> lookup;

        CacheLoader(HashMap<String, Deque<ReactorSession>> lookup) {
            Objects.requireNonNull(lookup, "'lookup' cannot be null.");
            this.lookup = new HashMap<>(lookup.size());
            for (Map.Entry<String, Deque<ReactorSession>> e : lookup.entrySet()) {
                final String name = Objects.requireNonNull(e.getKey(), "'name' cannot be null.");
                final Deque<ReactorSession> sessions
                    = Objects.requireNonNull(e.getValue(), "'sessions' cannot be null.");
                if (sessions.isEmpty()) {
                    throw new IllegalArgumentException("lookup cannot have empty 'sessions'");
                }
                this.lookup.put(name, new ArrayDeque<>(sessions));
            }
        }

        @Override
        public ReactorSession load(ProtonSessionWrapper protonSession) {
            // TODO (anu): When removing v1, use 'ProtonSession' instead of ProtonSessionWrapper.
            final String name = protonSession.getName();
            final Deque<ReactorSession> sessions = lookup.get(name);
            if (sessions == null) {
                throw new IllegalStateException("lookup has no session mapping defined for the name " + name);
            }
            final ReactorSession session = sessions.poll();
            if (session == null) {
                throw new IllegalStateException("lookup has no more sessions left for the name " + name);
            }
            return session;
        }
    }
}
