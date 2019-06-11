// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpExceptionHandler;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.CBSNode;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReactorConnection extends EndpointStateNotifierBase implements AmqpConnection {
    private final ConcurrentMap<String, AmqpSession> sessionMap = new ConcurrentHashMap<>();
    private final AtomicBoolean hasConnection = new AtomicBoolean();

    private final String connectionId;
    private final Mono<CBSNode> cbsChannelMono;
    private final Mono<Connection> connectionMono;
    private final ConnectionHandler handler;
    private final ReactorHandlerProvider handlerProvider;
    private final ConnectionParameters connectionParameters;
    private final ReactorProvider reactorProvider;
    private final Scheduler scheduler;
    private final Disposable.Composite subscriptions;

    private ReactorExecutor executor;
    //TODO (conniey): handle failures and recreating the Reactor. Resubscribing the handlers, etc.
    private ReactorExceptionHandler reactorExceptionHandler;

    /**
     * Creates a new AMQP connection that uses proton-j.
     *
     * @param connectionId Identifier for the connection.
     * @param connectionParameters A set of options used to create the AMQP connection.
     * @param reactorProvider Provides proton-j Reactor instances.
     * @param handlerProvider Provides {@link BaseHandler} to listen to proton-j reactor events.
     */
    public ReactorConnection(String connectionId, ConnectionParameters connectionParameters, ReactorProvider reactorProvider,
                             ReactorHandlerProvider handlerProvider) {
        super(new ServiceLogger(ReactorConnection.class));

        this.reactorProvider = reactorProvider;
        this.connectionParameters = connectionParameters;
        this.scheduler = connectionParameters.scheduler();
        this.connectionId = connectionId;
        this.handlerProvider = handlerProvider;

        this.handler = handlerProvider.createConnectionHandler(connectionId, connectionParameters.host(), connectionParameters.transportType());
        this.connectionMono = Mono.fromCallable(this::createConnectionAndStart)
            .doOnSubscribe(c -> {
                logger.asInfo().log("Creating and starting connection to {}:{}", handler.getHostname(), handler.getProtocolPort());
                hasConnection.set(true);
            }).cache();

        this.subscriptions = Disposables.composite(
            this.handler.getEndpointStates().subscribe(
                this::notifyEndpointState,
                error -> notifyError(new ErrorContext(error, getHost())),
                () -> notifyEndpointState(EndpointState.CLOSED)),
            this.handler.getErrors().subscribe(
                this::notifyError,
                error -> notifyError(new ErrorContext(error, getHost())),
                () -> notifyEndpointState(EndpointState.CLOSED)));

        this.cbsChannelMono = connectionMono.then(
            Mono.fromCallable(() -> new CBSChannel(this, connectionParameters.tokenProvider(), reactorProvider.getReactorDispatcher())));
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
    public Map<String, Object> getConnectionProperties() {
        return handler.getConnectionProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpSession> createSession(String sessionName) {
        return connectionMono.map(connection -> sessionMap.computeIfAbsent(sessionName, key -> {
            final SessionHandler handler = handlerProvider.createSessionHandler(connectionId, getHost(), sessionName, connectionParameters.timeout());
            final Session session = connection.session();

            BaseHandler.setHandler(session, handler);
            return new ReactorSession(session, handler, sessionName, reactorProvider.getReactorDispatcher(), connectionParameters.timeout());
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
        final Reactor reactor = reactorProvider.createReactor(connectionId, handler.getMaxFrameSize());
        final Connection connection = reactor.connectionToHost(handler.getHostname(), handler.getProtocolPort(), handler);

        reactorExceptionHandler = new ReactorExceptionHandler();
        executor = new ReactorExecutor(reactor, scheduler, connectionId, reactorExceptionHandler, connectionParameters.timeout());
        executor.start();

        return connection;
    }

    private static class ReactorExceptionHandler extends AmqpExceptionHandler {
        @Override
        public void onConnectionError(Throwable exception) {
            super.onConnectionError(exception);
        }

        @Override
        public void onConnectionError(ErrorContext context) {
            super.onConnectionError(context);
        }
    }
}
