// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpExceptionHandler;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.CBSNode;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.handler.ConnectionHandler;
import com.azure.messaging.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class ReactorConnection extends EndpointStateNotifierBase implements EventHubConnection {
    private static final AtomicReferenceFieldUpdater<ReactorConnection, CBSChannel> CBS_CHANNEL_FIELD_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(ReactorConnection.class, CBSChannel.class, "cbsChannel");
    private static final AtomicReferenceFieldUpdater<ReactorConnection, Connection> CONNECTION_FIELD_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(ReactorConnection.class, Connection.class, "connection");

    private volatile CBSChannel cbsChannel;
    private volatile Connection connection;

    private final ConcurrentMap<String, AmqpSession> sessionMap = new ConcurrentHashMap<>();
    private final AtomicBoolean hasConnection = new AtomicBoolean();

    private final String connectionId;
    private final Mono<Connection> connectionMono;
    private final ConnectionHandler handler;
    private final ReactorHandlerProvider handlerProvider;
    private final ConnectionOptions connectionOptions;
    private final ReactorProvider reactorProvider;
    private final Disposable.Composite subscriptions;
    private final Mono<EventHubManagementNode> managementChannelMono;
    private final TokenResourceProvider tokenResourceProvider;

    private ReactorExecutor executor;
    //TODO (conniey): handle failures and recreating the Reactor. Resubscribing the handlers, etc.
    private ReactorExceptionHandler reactorExceptionHandler;

    /**
     * Creates a new AMQP connection that uses proton-j.
     *
     * @param connectionId Identifier for the connection.
     * @param connectionOptions A set of options used to create the AMQP connection.
     * @param reactorProvider Provides proton-j Reactor instances.
     * @param handlerProvider Provides {@link BaseHandler} to listen to proton-j reactor events.
     */
    public ReactorConnection(String connectionId, ConnectionOptions connectionOptions,
                             ReactorProvider reactorProvider, ReactorHandlerProvider handlerProvider, AmqpResponseMapper mapper) {
        super(new ClientLogger(ReactorConnection.class));

        this.connectionOptions = connectionOptions;
        this.reactorProvider = reactorProvider;
        this.connectionId = connectionId;
        this.handlerProvider = handlerProvider;
        this.handler = handlerProvider.createConnectionHandler(connectionId, connectionOptions.host(), connectionOptions.transportType());

        this.connectionMono = Mono.fromCallable(() -> {
            if (CONNECTION_FIELD_UPDATER.compareAndSet(this, null, this.createConnectionAndStart())) {
                logger.asInfo().log("Creating and starting connection to {}:{}", handler.getHostname(), handler.getProtocolPort());
            }

            return CONNECTION_FIELD_UPDATER.get(this);
        }).doOnSubscribe(c -> hasConnection.set(true));

        this.subscriptions = Disposables.composite(
            this.handler.getEndpointStates().subscribe(
                this::notifyEndpointState,
                this::notifyError,
                () -> notifyEndpointState(EndpointState.CLOSED)),
            this.handler.getErrors().subscribe(
                this::notifyError,
                this::notifyError,
                () -> notifyEndpointState(EndpointState.CLOSED)));

        tokenResourceProvider = new TokenResourceProvider(connectionOptions.authorizationType(), connectionOptions.host());

        this.managementChannelMono = connectionMono.then(
            Mono.fromCallable(() -> (EventHubManagementNode) new ManagementChannel(this,
                connectionOptions.eventHubPath(), connectionOptions.tokenCredential(), tokenResourceProvider,
                reactorProvider, handlerProvider, mapper))).cache();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<CBSNode> getCBSNode() {
        final Mono<CBSNode> cbsNodeMono = getConnectionStates().takeUntil(x -> x == AmqpEndpointState.ACTIVE)
            .timeout(connectionOptions.timeout())
            .then(Mono.fromCallable(() -> {
                if (CBS_CHANNEL_FIELD_UPDATER.compareAndSet(this, null,
                    new CBSChannel(this, connectionOptions.tokenCredential(),
                        connectionOptions.authorizationType(), reactorProvider, handlerProvider,
                        connectionOptions.timeout()))) {
                    logger.asInfo().log("Setting CBS channel.");
                }

                return CBS_CHANNEL_FIELD_UPDATER.get(this);
            }));

        return hasConnection.get()
            ? cbsNodeMono
            : connectionMono.then(cbsNodeMono);
    }

    @Override
    public Mono<EventHubManagementNode> getManagementNode() {
        return managementChannelMono;
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
        AmqpSession existingSession = sessionMap.get(sessionName);
        if (existingSession != null) {
            return Mono.just(existingSession);
        }

        return connectionMono.map(connection -> sessionMap.computeIfAbsent(sessionName, key -> {
            final SessionHandler handler =
                handlerProvider.createSessionHandler(connectionId, getHost(), sessionName, connectionOptions.timeout());
            final Session session = connection.session();

            BaseHandler.setHandler(session, handler);
            return new ReactorSession(session, handler, sessionName, reactorProvider, handlerProvider,
                this.getCBSNode(), tokenResourceProvider, connectionOptions.timeout());
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

    private Connection createConnectionAndStart() throws IOException {
        final Reactor reactor = reactorProvider.createReactor(connectionId, handler.getMaxFrameSize());
        final Connection connection = reactor.connectionToHost(handler.getHostname(), handler.getProtocolPort(), handler);

        reactorExceptionHandler = new ReactorExceptionHandler();
        executor = new ReactorExecutor(reactor, connectionOptions.scheduler(), connectionId, reactorExceptionHandler,
            connectionOptions.timeout(), connectionOptions.host());

        executor.start();

        return connection;
    }

    private static final class ReactorExceptionHandler extends AmqpExceptionHandler {
        private ReactorExceptionHandler() {
            super(new ClientLogger(ReactorExceptionHandler.class));
        }

        @Override
        public void onConnectionError(Throwable exception) {
            super.onConnectionError(exception);
        }
    }
}
