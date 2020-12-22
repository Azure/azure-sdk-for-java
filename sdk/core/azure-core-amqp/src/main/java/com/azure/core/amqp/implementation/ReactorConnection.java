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
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.Reactor;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;

public class ReactorConnection implements AmqpConnection {
    private static final String CBS_SESSION_NAME = "cbs-session";
    private static final String CBS_ADDRESS = "$cbs";
    private static final String CBS_LINK_NAME = "cbs";

    private final ClientLogger logger = new ClientLogger(ReactorConnection.class);
    private final ConcurrentMap<String, SessionSubscription> sessionMap = new ConcurrentHashMap<>();

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final DirectProcessor<AmqpShutdownSignal> shutdownSignals = DirectProcessor.create();
    private final FluxSink<AmqpShutdownSignal> shutdownSignalsSink = shutdownSignals.sink();
    private final ReplayProcessor<AmqpEndpointState> endpointStates;

    private final String connectionId;
    private final Mono<Connection> connectionMono;
    private final ConnectionHandler handler;
    private final ReactorHandlerProvider handlerProvider;
    private final TokenManagerProvider tokenManagerProvider;
    private final MessageSerializer messageSerializer;
    private final ConnectionOptions connectionOptions;
    private final ReactorProvider reactorProvider;
    private final AmqpRetryPolicy retryPolicy;
    private final SenderSettleMode senderSettleMode;
    private final ReceiverSettleMode receiverSettleMode;

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
     * @param senderSettleMode to set as {@link SenderSettleMode} on sender.
     * @param receiverSettleMode to set as {@link ReceiverSettleMode} on receiver.
     */
    public ReactorConnection(String connectionId, ConnectionOptions connectionOptions, ReactorProvider reactorProvider,
        ReactorHandlerProvider handlerProvider, TokenManagerProvider tokenManagerProvider,
        MessageSerializer messageSerializer, String product, String clientVersion, SenderSettleMode senderSettleMode,
        ReceiverSettleMode receiverSettleMode) {

        this.connectionOptions = connectionOptions;
        this.reactorProvider = reactorProvider;
        this.connectionId = connectionId;
        this.handlerProvider = handlerProvider;
        this.tokenManagerProvider = Objects.requireNonNull(tokenManagerProvider,
            "'tokenManagerProvider' cannot be null.");
        this.messageSerializer = messageSerializer;
        this.handler = handlerProvider.createConnectionHandler(connectionId,
            connectionOptions.getFullyQualifiedNamespace(), connectionOptions.getTransportType(),
            connectionOptions.getProxyOptions(), product, clientVersion, connectionOptions.getSslVerifyMode(),
            connectionOptions.getClientOptions());

        this.retryPolicy = RetryUtil.getRetryPolicy(connectionOptions.getRetry());
        this.senderSettleMode = senderSettleMode;
        this.receiverSettleMode = receiverSettleMode;

        this.connectionMono = Mono.fromCallable(this::getOrCreateConnection);

        this.endpointStates = this.handler.getEndpointStates()
            .takeUntilOther(shutdownSignals)
            .map(state -> {
                logger.verbose("connectionId[{}]: State {}", connectionId, state);
                return AmqpEndpointStateUtil.getConnectionState(state);
            }).subscribeWith(ReplayProcessor.cacheLastOrDefault(AmqpEndpointState.UNINITIALIZED));
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
            return Mono.error(logger.logExceptionAsError(new IllegalStateException(String.format(
                "connectionId[%s]: Connection is disposed. Cannot get CBS node.", connectionId))));
        }

        final Mono<ClaimsBasedSecurityNode> cbsNodeMono =
            RetryUtil.withRetry(getEndpointStates().takeUntil(x -> x == AmqpEndpointState.ACTIVE),
                connectionOptions.getRetry().getTryTimeout(), retryPolicy)
            .then(Mono.fromCallable(this::getOrCreateCBSNode));

        return connectionMono.then(cbsNodeMono);
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
                            logger.info("connectionId[{}] sessionName[{}]: Error occurred. Removing and disposing"
                                    + " session.", connectionId, sessionName, error);
                            removeSession(key);
                        }, () -> {
                            logger.info("connectionId[{}] sessionName[{}]: Complete. Removing and disposing session.",
                                connectionId, sessionName);
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
            connectionOptions.getRetry().getTryTimeout(), retryPolicy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeSession(String sessionName) {
        return removeSession(sessionName, null);
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
        dispose(null);
        shutdownSignalsSink.next(new AmqpShutdownSignal(false, true,
            "Disposed by client."));
    }

    void dispose(ErrorCondition errorCondition) {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        logger.info("connectionId[{}], errorCondition[{}]: Disposing of ReactorConnection.", connectionId,
            errorCondition != null ? errorCondition : NOT_APPLICABLE);

        final String[] keys = sessionMap.keySet().toArray(new String[0]);
        for (String key : keys) {
            logger.info("connectionId[{}]: Removing session '{}'", connectionId, key);
            removeSession(key, errorCondition);
        }

        if (connection != null) {
            connection.close();
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

        final Flux<RequestResponseChannel> createChannel = createSession(sessionName).cast(ReactorSession.class)
            .map(reactorSession -> new RequestResponseChannel(getId(), getFullyQualifiedNamespace(), linkName,
                entityPath, reactorSession.session(), connectionOptions.getRetry(), handlerProvider, reactorProvider,
                messageSerializer, senderSettleMode, receiverSettleMode))
            .doOnNext(e -> {
                logger.info("Emitting new response channel. connectionId: {}. entityPath: {}. linkName: {}.",
                    getId(), entityPath, linkName);
            })
            .repeat();

        return createChannel.subscribeWith(new AmqpChannelProcessor<>(connectionId, entityPath,
            channel -> channel.getEndpointStates(), retryPolicy,
            new ClientLogger(RequestResponseChannel.class)));
    }

    private boolean removeSession(String sessionName, ErrorCondition errorCondition) {
        if (sessionName == null) {
            return false;
        }

        final SessionSubscription removed = sessionMap.remove(sessionName);

        if (removed != null) {
            removed.dispose(errorCondition);
        }

        return removed != null;
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
            logger.info("connectionId[{}]: Creating and starting connection to {}:{}", connectionId,
                handler.getHostname(), handler.getProtocolPort());

            final Reactor reactor = reactorProvider.createReactor(connectionId, handler.getMaxFrameSize());
            connection = reactor.connectionToHost(handler.getHostname(), handler.getProtocolPort(), handler);

            reactorExceptionHandler = new ReactorExceptionHandler();
            // Use a new single-threaded scheduler for this connection as QPID's Reactor is not thread-safe.
            // Using Schedulers.single() will use the same thread for all connections in this process which
            // limits the scalability of the no. of concurrent connections a single process can have.
            Scheduler scheduler = Schedulers.newSingle("reactor-executor");
            executor = new ReactorExecutor(reactor, scheduler, connectionId,
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
            ReactorConnection.this.dispose();
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

            dispose(new ErrorCondition(Symbol.getSymbol("onReactorError"), shutdownSignal.toString()));
            shutdownSignalsSink.next(shutdownSignal);
        }
    }

    private static final class SessionSubscription {
        private final AtomicBoolean isDisposed = new AtomicBoolean();
        private final AmqpSession session;
        private final Disposable subscription;

        private SessionSubscription(AmqpSession session, Disposable subscription) {
            this.session = session;
            this.subscription = subscription;
        }

        public AmqpSession getSession() {
            return session;
        }

        void dispose(ErrorCondition errorCondition) {
            if (isDisposed.getAndSet(true)) {
                return;
            }

            if (session instanceof ReactorSession) {
                final ReactorSession reactorSession = (ReactorSession) session;
                reactorSession.dispose(errorCondition);
            } else {
                session.dispose();
            }
            subscription.dispose();
        }
    }
}
