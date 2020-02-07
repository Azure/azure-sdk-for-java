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
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.Reactor;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
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
    private final ConcurrentMap<String, SessionSubscription> sessionMap = new ConcurrentHashMap<>();

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
     * @param messageSerializer Serializer to translate objects to and from proton-j {@link Message messages}.
     * @param product The name of the product this connection is created for.
     * @param clientVersion The version of the client library creating the connection.
     */
    public ReactorConnection(String connectionId, ConnectionOptions connectionOptions, ReactorProvider reactorProvider,
        ReactorHandlerProvider handlerProvider, TokenManagerProvider tokenManagerProvider,
        MessageSerializer messageSerializer, String product, String clientVersion) {

        this.connectionOptions = connectionOptions;
        this.reactorProvider = reactorProvider;
        this.connectionId = connectionId;
        this.handlerProvider = handlerProvider;
        this.tokenManagerProvider = Objects.requireNonNull(tokenManagerProvider,
            "'tokenManagerProvider' cannot be null.");
        this.messageSerializer = messageSerializer;
        this.handler = handlerProvider.createConnectionHandler(connectionId,
            connectionOptions.getFullyQualifiedNamespace(), connectionOptions.getTransportType(),
            connectionOptions.getProxyOptions(), product, clientVersion);
        this.retryPolicy = RetryUtil.getRetryPolicy(connectionOptions.getRetry());

        this.connectionMono = Mono.fromCallable(this::getOrCreateConnection)
            .doOnSubscribe(c -> hasConnection.set(true));

        this.subscriptions = Disposables.composite(
            this.handler.getEndpointStates().subscribe(
                state -> {
                    logger.verbose("Connection state: {}", state);
                    endpointStatesSink.next(AmqpEndpointStateUtil.getConnectionState(state));
                }, error -> {
                    logger.error("connectionId[{}] Error occurred in connection endpoint.", connectionId, error);
                    endpointStatesSink.error(error);
                }, () -> {
                    endpointStatesSink.next(AmqpEndpointState.CLOSED);
                    endpointStatesSink.complete();
                }),

            this.handler.getErrors().subscribe(error -> {
                logger.error("connectionId[{}] Error occurred in connection handler.", connectionId, error);
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
        if (isDisposed()) {
            return Mono.error(logger.logExceptionAsError(new IllegalStateException(
                "Connection is disposed. Cannot get CBS node.")));
        }

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
        if (isDisposed()) {
            return Mono.error(logger.logExceptionAsError(new IllegalStateException(String.format(
                "connectionId[%s]: Connection is disposed. Cannot create session '%s'.", connectionId, sessionName))));
        }

        final SessionSubscription existing = sessionMap.get(sessionName);
        if (existing != null) {
            return Mono.just(existing.getSession());
        }

        return connectionMono.map(connection -> {
            final SessionSubscription sessionSubscription = sessionMap.computeIfAbsent(sessionName, key -> {
                final SessionHandler handler = handlerProvider.createSessionHandler(connectionId,
                    getFullyQualifiedNamespace(), key, connectionOptions.getRetry().getTryTimeout());
                final Session session = connection.session();

                BaseHandler.setHandler(session, handler);
                final AmqpSession amqpSession = createSession(key, session, handler);
                final Disposable subscription = amqpSession.getEndpointStates()
                    .subscribe(state -> {
                    }, error -> {
                            logger.info("sessionName[{}]: Error occurred. Removing and disposing session.",
                                sessionName, error);
                            removeSession(key);
                        }, () -> {
                            logger.info("sessionName[{}]: Complete. Removing and disposing session.", sessionName);
                            removeSession(key);
                        });

                return new SessionSubscription(amqpSession, subscription);
            });

            return sessionSubscription.getSession();
        });
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
        if (sessionName == null) {
            return false;
        }

        final SessionSubscription removed = sessionMap.remove(sessionName);

        if (removed != null) {
            removed.dispose();
        }

        return removed != null;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        subscriptions.dispose();
        endpointStatesSink.complete();

        final String[] keys = sessionMap.keySet().toArray(new String[0]);
        for (String key : keys) {
            removeSession(key);
        }

        if (executor != null) {
            executor.close();
        }
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
     *
     * @return A new {@link RequestResponseChannel} to communicate with the message broker.
     */
    protected Mono<RequestResponseChannel> createRequestResponseChannel(String sessionName, String linkName,
        String entityPath) {
        final Flux<RequestResponseChannel> createChannel = createSession(sessionName)
            .cast(ReactorSession.class)
            .map(reactorSession -> new RequestResponseChannel(getId(), getFullyQualifiedNamespace(), linkName,
                entityPath, reactorSession.session(), connectionOptions.getRetry(), handlerProvider, reactorProvider,
                messageSerializer)).repeat();

        return createChannel.subscribeWith(new AmqpChannelProcessor<>(connectionId, entityPath,
            channel -> channel.getEndpointStates(), retryPolicy,
            new ClientLogger(RequestResponseChannel.class + "<" + sessionName + ">")));
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
            executor = new ReactorExecutor(reactor, Schedulers.single(), connectionId,
                reactorExceptionHandler, connectionOptions.getRetry().getTryTimeout(),
                connectionOptions.getFullyQualifiedNamespace());

            executor.start();
        }

        return connection;
    }

    private final class ReactorExceptionHandler extends AmqpExceptionHandler {
        private ReactorExceptionHandler() {
            super();
        }

        @Override
        public void onConnectionError(Throwable exception) {
            if (isDisposed.get()) {
                super.onConnectionError(exception);
                return;
            }

            logger.warning(
                "onReactorError connectionId[{}], hostName[{}], message[Starting new reactor], error[{}]",
                getId(), getFullyQualifiedNamespace(), exception.getMessage());

            endpointStates.onError(exception);
        }

        @Override
        void onConnectionShutdown(AmqpShutdownSignal shutdownSignal) {
            if (isDisposed()) {
                super.onConnectionShutdown(shutdownSignal);
                return;
            }

            logger.warning(
                "onReactorError connectionId[{}], hostName[{}], message[Shutting down], shutdown signal[{}]",
                getId(), getFullyQualifiedNamespace(), shutdownSignal.isInitiatedByClient(), shutdownSignal);

            if (!endpointStatesSink.isCancelled()) {
                endpointStatesSink.next(AmqpEndpointState.CLOSED);
                endpointStatesSink.complete();
            }

            dispose();
        }
    }

    private static final class SessionSubscription implements Disposable {
        private final AtomicBoolean isDisposed = new AtomicBoolean();
        private final AmqpSession session;
        private final Disposable subscription;

        private SessionSubscription(AmqpSession session, Disposable subscription) {
            this.session = session;
            this.subscription = subscription;
        }

        public Disposable getSubscription() {
            return subscription;
        }

        public AmqpSession getSession() {
            return session;
        }

        @Override
        public void dispose() {
            if (isDisposed.getAndSet(true)) {
                return;
            }

            subscription.dispose();
            session.dispose();
        }
    }
}
