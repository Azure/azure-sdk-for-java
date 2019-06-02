// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.ExceptionHandler;
import com.azure.core.amqp.ShutdownSignal;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.handler.ConnectionHandler;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReactorConnection extends StateNotifierBase implements AmqpConnection {
    //TODO (conniey): Change this to use our configuration settings.
    private static final Duration DEFAULT_OPERATION_TIMEOUT = Duration.ofSeconds(60);

    private final ConcurrentMap<String, AmqpSession> sessionMap = new ConcurrentHashMap<>();
    private final AtomicBoolean hasConnection = new AtomicBoolean();

    private final ConnectionHandler handler;
    private final Disposable.Composite subscriptions;
    private final Mono<Connection> connectionMono;
    private final Scheduler scheduler;
    private final ReactorProvider provider;
    private final Mono<CBSNode> cbsChannelMono;
    private final String connectionId;

    private ReactorExecutor executor;
    //TODO (conniey): handle failures and recreating the Reactor. Resubscribing the handlers, etc.
    private ReactorExceptionHandler reactorExceptionHandler;

    ReactorConnection(String connectionId, ConnectionHandler handler, Scheduler scheduler, ReactorProvider provider,
                      TokenProvider tokenProvider) {
        super(new ServiceLogger(ReactorConnection.class));

        Objects.requireNonNull(connectionId);
        Objects.requireNonNull(handler);
        Objects.requireNonNull(scheduler);
        Objects.requireNonNull(provider);
        Objects.requireNonNull(tokenProvider);

        this.provider = provider;
        this.scheduler = scheduler;
        this.connectionId = connectionId;
        this.handler = handler;
        this.connectionMono = Mono.fromCallable(this::createConnectionAndStart)
            .doOnSubscribe(c -> {
                logger.asInformational().log("Creating and starting connection to {}:{}", handler.getHostname(), handler.protocolPort());
                hasConnection.set(true);
            }).cache();
        this.subscriptions = Disposables.composite(
            this.handler.getEndpointStates().subscribe(
                this::notifyAndSetConnectionState,
                error -> notifyException(new ErrorContext(error, getHost())),
                () -> notifyAndSetConnectionState(EndpointState.CLOSED)),
            this.handler.getErrors().subscribe(
                this::notifyException,
                error -> notifyException(new ErrorContext(error, getHost())),
                () -> notifyAndSetConnectionState(EndpointState.CLOSED)));

        this.cbsChannelMono = connectionMono.then(
            Mono.fromCallable(() -> new CBSChannel(this, tokenProvider, provider.getReactorDispatcher())));
    }

    /**
     * Creates a new AMQP connection that uses proton-j.
     *
     * @param connectionId Identifier for the connection.
     * @param hostname Host it is connecting to.
     * @param scheduler The scheduler for this AmqpConnection
     * @param provider Provider that creates Reactor instances.
     */
    public static AmqpConnection create(String connectionId, String hostname, TokenProvider tokenProvider, Scheduler scheduler, ReactorProvider provider) {
        final ConnectionHandler handler = new ConnectionHandler(connectionId, hostname);
        return new ReactorConnection(connectionId, handler, scheduler, provider, tokenProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<CBSNode> getCBSNode() {
        return cbsChannelMono;
    }

    @Override
    public String getIdentifier() {
        return connectionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHost() {
        return handler.getHostname();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxFrameSize() {
        return handler.getMaxFrameSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getClientProperties() {
        return handler.connectionProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpSession> createSession(String sessionName) {
        return connectionMono.map(connection -> sessionMap.computeIfAbsent(sessionName, key -> {
            final SessionHandler handler = new SessionHandler(connectionId, getHost(), sessionName,
                provider.getReactorDispatcher(), DEFAULT_OPERATION_TIMEOUT);
            final Session session = connection.session();

            BaseHandler.setHandler(session, handler);
            return new ReactorSession(session, handler, sessionName, provider.getReactorDispatcher(), DEFAULT_OPERATION_TIMEOUT);
        }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeSession(String sessionName) {
        return sessionName != null && sessionMap.remove(sessionName) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (executor != null) {
            executor.close();
        }

        subscriptions.dispose();
        sessionMap.forEach((name, session) -> {
            try {
                session.close();
            } catch (IOException e) {
                logger.asError().log("Could not close session: " + name, e);
            }
        });
        super.close();
    }


    private synchronized Connection createConnectionAndStart() throws IOException {
        final Reactor reactor = provider.createReactor(connectionId, handler.getMaxFrameSize());
        final Connection connection = reactor.connectionToHost(handler.getHostname(), handler.protocolPort(), handler);

        reactorExceptionHandler = new ReactorExceptionHandler();
        executor = new ReactorExecutor(reactor, scheduler, connectionId, reactorExceptionHandler, DEFAULT_OPERATION_TIMEOUT);
        executor.start();

        return connection;
    }

    private static class ReactorExceptionHandler extends ExceptionHandler {
        @Override
        public void onConnectionError(Throwable exception) {
            super.onConnectionError(exception);
        }

        @Override
        public void onConnectionError(ErrorContext context) {
            super.onConnectionError(context);
        }

        @Override
        public void onConnectionShutdown(ShutdownSignal shutdownSignal) {
            super.onConnectionShutdown(shutdownSignal);
        }
    }
}
