// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReactorConnection implements AmqpConnection {
    private static final String CBS_SESSION_NAME = "cbs-session";
    private static final String CBS_ADDRESS = "$cbs";
    private static final String CBS_LINK_NAME = "cbs";

    private final ClientLogger logger = new ClientLogger(ReactorConnection.class);
    private final ConcurrentMap<String, AmqpSession> sessionMap = new ConcurrentHashMap<>();
    private final AtomicBoolean hasConnection = new AtomicBoolean();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final DirectProcessor<AmqpShutdownSignal> shutdownSignals = DirectProcessor.create();
    private final ReplayProcessor<AmqpEndpointState> endpointStates =
        ReplayProcessor.cacheLastOrDefault(AmqpEndpointState.UNINITIALIZED);
    private FluxSink<AmqpEndpointState> endpointStatesSink = endpointStates.sink(FluxSink.OverflowStrategy.BUFFER);

    private final String connectionId;
    private final Mono<Connection> connectionMono;
    private final ConnectionHandler handler;
    private final ReactorHandlerProvider handlerProvider;
    private final TokenManagerProvider tokenManagerProvider;
    private final MessageSerializer messageSerializer;
    private final ConnectionOptions connectionOptions;
    private final ReactorProvider reactorProvider;
    private final Disposable.Composite subscriptions;
    private final AmqpRetryPolicy retryPolicy;

    private ReactorExecutor executor;
    //TODO (conniey): handle failures and recreating the Reactor. Resubscribing the handlers, etc.
    private ReactorExceptionHandler reactorExceptionHandler;

    private volatile ClaimsBasedSecurityChannel cbsChannel;
    private volatile Connection connection;

    /**
     * Creates a new AMQP connection that uses proton-j.
     *
     * @param connectionId Identifier for the connection.
     * @param connectionOptions A set of options used to create the AMQP connection.
     * @param reactorProvider Provides proton-j Reactor instances.
     * @param handlerProvider Provides {@link BaseHandler} to listen to proton-j reactor events.
     * @param tokenManagerProvider Provides the appropriate token manager to authorize with CBS node.
     */
    public ReactorConnection(String connectionId, ConnectionOptions connectionOptions, ReactorProvider reactorProvider,
                             ReactorHandlerProvider handlerProvider, TokenManagerProvider tokenManagerProvider,
                             MessageSerializer messageSerializer) {

        this.connectionOptions = connectionOptions;
        this.reactorProvider = reactorProvider;
        this.connectionId = connectionId;
        this.handlerProvider = handlerProvider;
        this.tokenManagerProvider = Objects.requireNonNull(tokenManagerProvider,
            "'tokenManagerProvider' cannot be null.");
        this.messageSerializer = messageSerializer;
        this.handler = handlerProvider.createConnectionHandler(connectionId,
            connectionOptions.getFullyQualifiedNamespace(), connectionOptions.getTransportType(),
            connectionOptions.getProxyOptions());
        this.retryPolicy = RetryUtil.getRetryPolicy(connectionOptions.getRetry());

        this.connectionMono = Mono.fromCallable(this::getOrCreateConnection)
            .doOnSubscribe(c -> hasConnection.set(true));

        this.subscriptions = Disposables.composite(
            this.handler.getEndpointStates().subscribe(
                state -> {
                    logger.verbose("Connection state: {}", state);
                    endpointStatesSink.next(AmqpEndpointStateUtil.getConnectionState(state));
                }, error -> {
                    logger.error("Error occurred in connection.", error);
                    endpointStatesSink.error(error);
                }, () -> {
                    endpointStatesSink.next(AmqpEndpointState.CLOSED);
                    endpointStatesSink.complete();
                }),

            this.handler.getErrors().subscribe(error -> {
                logger.error("Error occurred in connection.", error);
                endpointStatesSink.error(error);
            }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<AmqpEndpointState> getEndpointStates() {
        return endpointStates;
    }

    @Override
    public Flux<AmqpShutdownSignal> getShutdownSignals() {
        return shutdownSignals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ClaimsBasedSecurityNode> getClaimsBasedSecurityNode() {
        final Mono<ClaimsBasedSecurityNode> cbsNodeMono = RetryUtil.withRetry(
            getEndpointStates().takeUntil(x -> x == AmqpEndpointState.ACTIVE),
            connectionOptions.getRetry().getTryTimeout(), retryPolicy)
            .then(Mono.fromCallable(this::getOrCreateCBSNode));

        return hasConnection.get()
            ? cbsNodeMono
            : connectionMono.then(cbsNodeMono);
    }

    @Override
    public String getId() {
        return connectionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullyQualifiedNamespace() {
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
            final SessionHandler handler = handlerProvider.createSessionHandler(connectionId,
                getFullyQualifiedNamespace(), sessionName, connectionOptions.getRetry().getTryTimeout());
            final Session session = connection.session();

            BaseHandler.setHandler(session, handler);
            return createSession(sessionName, session, handler);
        }));
    }

    /**
     * Creates a new AMQP session with the given parameters.
     *
     * @param sessionName Name of the AMQP session.
     * @param session The reactor session associated with this session.
     * @param handler Session handler for the reactor session.
     *
     * @return A new instance of AMQP session.
     */
    protected AmqpSession createSession(String sessionName, Session session, SessionHandler handler) {
        return new ReactorSession(session, handler, sessionName, reactorProvider, handlerProvider,
            getClaimsBasedSecurityNode(), tokenManagerProvider, messageSerializer,
            connectionOptions.getRetry().getTryTimeout());
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
        if (isDisposed.getAndSet(true)) {
            return;
        }

        if (executor != null) {
            executor.close();
        }

        subscriptions.dispose();
        endpointStatesSink.complete();

        final HashMap<String, AmqpSession> map = new HashMap<>(sessionMap);

        sessionMap.clear();
        map.forEach((name, session) -> session.close());
    }

    /**
     * Gets the AMQP connection for this instance.
     *
     * @return The AMQP connection.
     */
    protected Mono<Connection> getReactorConnection() {
        return connectionMono;
    }

    /**
     * Creates a bidirectional link between the message broker and the client.
     *
     * @param sessionName Name of the session.
     * @param linkName Name of the link.
     * @param entityPath Address to the message broker.
     * @return A new {@link RequestResponseChannel} to communicate with the message broker.
     */
    protected Mono<RequestResponseChannel> createRequestResponseChannel(String sessionName, String linkName,
            String entityPath) {
        return createSession(sessionName)
            .cast(ReactorSession.class)
            .map(reactorSession -> new RequestResponseChannel(getId(), getFullyQualifiedNamespace(), linkName,
                entityPath, reactorSession.session(), connectionOptions.getRetry(), handlerProvider, reactorProvider,
                messageSerializer));
    }

    private synchronized ClaimsBasedSecurityNode getOrCreateCBSNode() {
        if (cbsChannel == null) {
            logger.info("Setting CBS channel.");

            cbsChannel = new ClaimsBasedSecurityChannel(
                createRequestResponseChannel(CBS_SESSION_NAME, CBS_LINK_NAME, CBS_ADDRESS),
                connectionOptions.getTokenCredential(), connectionOptions.getAuthorizationType(),
                connectionOptions.getRetry());
        }

        return cbsChannel;
    }

    private synchronized Connection getOrCreateConnection() throws IOException {
        if (connection == null) {
            logger.info("Creating and starting connection to {}:{}", handler.getHostname(), handler.getProtocolPort());

            final Reactor reactor = reactorProvider.createReactor(connectionId, handler.getMaxFrameSize());
            connection = reactor.connectionToHost(handler.getHostname(), handler.getProtocolPort(), handler);

            reactorExceptionHandler = new ReactorExceptionHandler();
            executor = new ReactorExecutor(reactor, connectionOptions.getScheduler(), connectionId,
                reactorExceptionHandler, connectionOptions.getRetry().getTryTimeout(),
                connectionOptions.getFullyQualifiedNamespace());

            executor.start();
        }

        return connection;
    }

    private static final class ReactorExceptionHandler extends AmqpExceptionHandler {
        private ReactorExceptionHandler() {
            super();
        }

        @Override
        public void onConnectionError(Throwable exception) {
            super.onConnectionError(exception);
        }
    }
}
