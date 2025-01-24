// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.Connection;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.amqp.implementation.ClientConstants.SESSION_ID_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.SESSION_NAME_KEY;

/**
 * A cache of {@link ReactorSession} instances owned by a {@link ReactorConnection}.
 */
final class ReactorSessionCache {
    private final ConcurrentMap<String, Entry> entries = new ConcurrentHashMap<>();
    private final String fullyQualifiedNamespace;
    private final String connectionId;
    private final ReactorHandlerProvider handlerProvider;
    private final ReactorProvider reactorProvider;
    private final Duration openTimeout;
    private final AtomicBoolean isOwnerDisposed;
    private final ClientLogger logger;

    /**
     * Creates the cache.
     *
     * @param connectionId the id of the {@link ReactorConnection} owning the cache.
     * @param fullyQualifiedNamespace the host name of the broker that the owner is connected to.
     * @param handlerProvider the handler provider for various type of endpoints (session, link).
     * @param reactorProvider the provider for reactor dispatcher to dispatch work to QPid Reactor thread.
     * @param openTimeout the session open timeout.
     * @param logger the client logger.
     */
    ReactorSessionCache(String connectionId, String fullyQualifiedNamespace, ReactorHandlerProvider handlerProvider,
        ReactorProvider reactorProvider, Duration openTimeout, ClientLogger logger) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.connectionId = connectionId;
        this.handlerProvider = handlerProvider;
        this.reactorProvider = reactorProvider;
        this.openTimeout = openTimeout;
        this.isOwnerDisposed = new AtomicBoolean(false);
        this.logger = logger;
    }

    /**
     * Obtain the session with the given name from the cache, first loading and opening the session if necessary.
     * <p>
     * The session returned from the cache will be already connected to the broker and ready to use.
     * </p>
     * <p>
     * A session will be evicted from the cache if it terminates (e.g., broker disconnected the session).
     * </p>
     *
     * @param connectionMono the Mono that emits QPid Proton-j {@link Connection} that host the session.
     * @param name the session name.
     * @param loader to load the session on cache miss, cache miss can happen if session is requested
     *  for the first time or previously loaded one was evicted.
     *
     * @return the session, that is active and connected to the broker.
     */
    Mono<ReactorSession> getOrLoad(Mono<Connection> connectionMono, String name, Loader loader) {
        final Mono<Entry> entryMono = connectionMono.map(connection -> {
            return entries.computeIfAbsent(name, sessionName -> {
                final ReactorSession session = load(connection, sessionName, loader);
                final Disposable disposable = setupAutoEviction(session);
                return new Entry(session, disposable);
            });
        });
        return entryMono.flatMap(entry -> {
            final ReactorSession session = entry.getSession();
            return session.open()
                .doOnError(error -> evict(session, "Evicting failed to open or in-active session.", error));
            //
            // Notes on session.open():
            //
            // 'ReactorSession::open()' has open-only-once semantics, where the open attempt (i.e., the internal call
            //  to org.apache.qpid.proton.engine.Session::open()) is triggered upon the first subscription that loads
            //  session into this cache. The one time internal open call will be executed on the QPid Reactor thread.
            //
            //  Later subscriptions only trigger the session active check (i.e., checks if the session is still
            //  connected to the broker).
            //
            //  For both first and later subscriptions, the 'ReactorSession::open()' will return only after the session
            //  is active, if the session is not active within the timeout configured via AmqpRetryOptions::tryTimeout,
            //  then the API will fail with timeout error.
        });
        //
        // Notes on eviction:
        //
        // 1. If the session is disconnected after the successful open, the auto-eviction that was set up
        // (via 'setupAutoEviction') when the session was loaded will take care of the eviction.
        // 2. If the initial open attempt itself fails or if the session transition to connected (active) state
        // fails with time out, then 'doOnError' (via 'evict') will take care of the eviction.
    }

    /**
     * Evicts the session from the cache.
     *
     * @param name the name of the session to evict.
     * @return true if the session was evicted, false if no session found with the given name.
     */
    boolean evict(String name) {
        if (name == null) {
            return false;
        }
        final Entry removed = entries.remove(name);
        if (removed != null) {
            removed.dispose();
        }
        return removed != null;
    }

    /**
     * Signal that the owner ({@link ReactorConnection}) of the cache is disposed of.
     */
    void setOwnerDisposed() {
        isOwnerDisposed.set(true);
    }

    /**
     * When the owner {@link ReactorConnection} is being disposed of, all {@link ReactorSession} loaded into the cache
     * will receive shutdown signal through the channel established at ReactorSession's construction time, the owner
     * may use this method to waits for sessions to complete it closing.
     *
     * @return a Mono that completes when all sessions are closed via owner shutdown signaling.
     */
    Mono<Void> awaitClose() {
        final ArrayList<Mono<Void>> closing = new ArrayList<>(entries.size());
        for (Entry entry : entries.values()) {
            closing.add(entry.awaitSessionClose());
        }
        return Mono.when(closing);
    }

    /**
     * Load a new {@link ReactorSession} to be cached.
     *
     * @param connection the QPid Proton-j connection to host the session.
     * @param name the session name.
     * @param loader the function to load the session.
     *
     * @return the session to cache.
     */
    private ReactorSession load(Connection connection, String name, Loader loader) {
        final ProtonSession protonSession = new ProtonSession(connectionId, fullyQualifiedNamespace, connection,
            handlerProvider, reactorProvider, name, openTimeout, logger);
        // TODO (anu): Update loader signature to use 'ProtonSession' instead of 'ProtonSessionWrapper' when removing v1.
        return loader.load(new ProtonSessionWrapper(protonSession));
    }

    /**
     * Register to evict the session from the cache when the session terminates.
     *
     * @param session the session to register for cache eviction.
     * @return the registration disposable.
     */
    private Disposable setupAutoEviction(ReactorSession session) {
        return session.getEndpointStates().subscribe(__ -> {
        }, error -> {
            evict(session, "Evicting session terminated with error.", error);
        }, () -> {
            evict(session, "Evicting terminated session.", null);
        });
    }

    /**
     * Attempt to evict the session from the cache.
     *
     * @param session the session to evict.
     * @param message the message to log on eviction.
     * @param error the error triggered the eviction.
     */
    private void evict(ReactorSession session, String message, Throwable error) {
        if (isOwnerDisposed.get()) {
            // If (owner) connection is already disposing of, all session(s) would be discarded. Which means the whole
            // cache itself would be discarded. In this case, don't evict the individual sessions from the cache, this
            // helps to prevent session recreation attempts by downstream while connection cleanup is running.
            return;
        }
        final String name = session.getSessionName();
        final String id = session.getId();
        if (error != null) {
            logger.atInfo().addKeyValue(SESSION_NAME_KEY, name).addKeyValue(SESSION_ID_KEY, id).log(message, error);
        } else {
            logger.atInfo().addKeyValue(SESSION_NAME_KEY, name).addKeyValue(SESSION_ID_KEY, id).log(message);
        }
        evict(name);
    }

    /**
     * Type to load a {@link ReactorSession} for caching it.
     */
    @FunctionalInterface
    interface Loader {
        /**
         * Load a {@link ReactorSession} for caching.
         *
         * @param protonSession the {@link ProtonSession} to back the loaded {@link ReactorSession}.
         * <p>
         * TODO (anu): When removing v1, update signature to use 'ProtonSession' instead of wrapper.
         * </p>
         *
         * @return the session to cache.
         */
        ReactorSession load(ProtonSessionWrapper protonSession);
    }

    /**
     * An entry in the cache holding {@link ReactorSession} and {@link Disposable} for the task to evict the entry
     * from the cache.
     */
    private static final class Entry extends AtomicBoolean {
        private final ReactorSession session;
        private final Disposable disposable;

        /**
         * Creates a cache entry.
         *
         * @param session the session to cache.
         * @param disposable the disposable to evict the session from the cache.
         */
        private Entry(ReactorSession session, Disposable disposable) {
            super(false);
            this.session = session;
            this.disposable = disposable;
        }

        /**
         * Gets the session cached in the entry.
         *
         * @return the session.
         */
        private ReactorSession getSession() {
            return session;
        }

        /**
         * Await for the cached session to close.
         *
         * @return a Mono that completes when the session is closed.
         */
        private Mono<Void> awaitSessionClose() {
            return session.isClosed();
        }

        /**
         * Dispose of the cached session and the eviction disposable.
         */
        private void dispose() {
            if (super.getAndSet(true)) {
                return;
            }
            session.closeAsync("closing session.", null, true).subscribe();
            disposable.dispose();
        }
    }
}
