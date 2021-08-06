// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpManagementNode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.Reactor;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An AMQP connection backed by proton-j.
 */
public class ReactorConnection implements AmqpConnection {
    private static final String CBS_SESSION_NAME = "cbs-session";
    private static final String CBS_ADDRESS = "$cbs";
    private static final String CBS_LINK_NAME = "cbs";

    private static final String MANAGEMENT_SESSION_NAME = "mgmt-session";
    private static final String MANAGEMENT_ADDRESS = "$management";
    private static final String MANAGEMENT_LINK_NAME = "mgmt";

    private final ClientLogger logger = new ClientLogger(ReactorConnection.class);
    private final ConcurrentMap<String, SessionSubscription> sessionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AmqpManagementNode> managementNodes = new ConcurrentHashMap<>();

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Sinks.One<AmqpShutdownSignal> shutdownSignalSink = Sinks.one();
    private final Flux<AmqpEndpointState> endpointStates;
    private final Sinks.Empty<Void> isClosedMono = Sinks.empty();

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
    private final Duration operationTimeout;
    private final Composite subscriptions;

    private ReactorExecutor executor;

    private volatile ClaimsBasedSecurityChannel cbsChannel;
    private volatile AmqpChannelProcessor<RequestResponseChannel> cbsChannelProcessor;
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
     * @param senderSettleMode to set as {@link SenderSettleMode} on sender.
     * @param receiverSettleMode to set as {@link ReceiverSettleMode} on receiver.
     */
    public ReactorConnection(String connectionId, ConnectionOptions connectionOptions, ReactorProvider reactorProvider,
        ReactorHandlerProvider handlerProvider, TokenManagerProvider tokenManagerProvider,
        MessageSerializer messageSerializer, SenderSettleMode senderSettleMode,
        ReceiverSettleMode receiverSettleMode) {

        this.connectionOptions = connectionOptions;
        this.reactorProvider = reactorProvider;
        this.connectionId = connectionId;
        this.handlerProvider = handlerProvider;
        this.tokenManagerProvider = Objects.requireNonNull(tokenManagerProvider,
            "'tokenManagerProvider' cannot be null.");
        this.messageSerializer = messageSerializer;
        this.handler = handlerProvider.createConnectionHandler(connectionId, connectionOptions);

        this.retryPolicy = RetryUtil.getRetryPolicy(connectionOptions.getRetry());
        this.operationTimeout = connectionOptions.getRetry().getTryTimeout();
        this.senderSettleMode = senderSettleMode;
        this.receiverSettleMode = receiverSettleMode;

        this.connectionMono = Mono.fromCallable(this::getOrCreateConnection)
            .flatMap(reactorConnection -> {
                final Mono<AmqpEndpointState> activeEndpoint = getEndpointStates()
                    .filter(state -> state == AmqpEndpointState.ACTIVE)
                    .next()
                    .timeout(operationTimeout, Mono.error(new AmqpException(true, String.format(
                        "Connection '%s' not opened within AmqpRetryOptions.tryTimeout(): %s", connectionId,
                        operationTimeout), handler.getErrorContext())));
                return activeEndpoint.thenReturn(reactorConnection);
            })
            .doOnError(error -> {
                final String message = String.format(
                    "connectionId[%s] Error occurred while connection was starting. Error: %s", connectionId, error);

                if (isDisposed.getAndSet(true)) {
                    logger.verbose("connectionId[{}] was already disposed. {}", connectionId, message);
                } else {
                    closeAsync(new AmqpShutdownSignal(false, false, message)).subscribe();
                }
            });

        this.endpointStates = this.handler.getEndpointStates()
            .takeUntilOther(shutdownSignalSink.asMono())
            .map(state -> {
                logger.verbose("connectionId[{}]: State {}", connectionId, state);
                return AmqpEndpointStateUtil.getConnectionState(state);
            })
            .onErrorResume(error -> {
                if (!isDisposed.getAndSet(true)) {
                    logger.verbose("connectionId[{}]: Disposing of active sessions due to error.", connectionId);
                    return closeAsync(new AmqpShutdownSignal(false, false,
                        error.getMessage())).then(Mono.error(error));
                } else {
                    return Mono.error(error);
                }
            })
            .doOnComplete(() -> {
                if (!isDisposed.getAndSet(true)) {
                    logger.verbose("connectionId[{}]: Disposing of active sessions due to connection close.",
                        connectionId);

                    closeAsync(new AmqpShutdownSignal(false, false,
                        "Connection handler closed.")).subscribe();
                }
            })
            .cache(1);

        this.subscriptions = Disposables.composite(this.endpointStates.subscribe());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<AmqpEndpointState> getEndpointStates() {
        return endpointStates;
    }

    /**
     * Gets the shutdown signal associated with this connection. When it emits, the underlying connection is closed.
     *
     * @return Shutdown signals associated with this connection. It emits a signal when the underlying connection is
     *     closed.
     */
    @Override
    public Flux<AmqpShutdownSignal> getShutdownSignals() {
        return shutdownSignalSink.asMono().cache().flux();
    }

    @Override
    public Mono<AmqpManagementNode> getManagementNode(String entityPath) {
        return Mono.defer(() -> {
            if (isDisposed()) {
                return Mono.error(logger.logExceptionAsError(new IllegalStateException(String.format(
                    "connectionId[%s]: Connection is disposed. Cannot get management instance for '%s'",
                    connectionId, entityPath))));
            }

            final AmqpManagementNode existing = managementNodes.get(entityPath);
            if (existing != null) {
                return Mono.just(existing);
            }

            final TokenManager tokenManager = new AzureTokenManagerProvider(connectionOptions.getAuthorizationType(),
                connectionOptions.getFullyQualifiedNamespace(), connectionOptions.getAuthorizationScope())
                .getTokenManager(getClaimsBasedSecurityNode(), entityPath);

            return tokenManager.authorize().thenReturn(managementNodes.compute(entityPath, (key, current) -> {
                if (current != null) {
                    logger.info("A management node exists already, returning it.");

                    // Close the token manager we had created during this because it is unneeded now.
                    tokenManager.close();
                    return current;
                }

                final String sessionName = entityPath + "-" + MANAGEMENT_SESSION_NAME;
                final String linkName = entityPath + "-" + MANAGEMENT_LINK_NAME;
                final String address = entityPath + "/" + MANAGEMENT_ADDRESS;

                logger.info("Creating management node. entityPath[{}], address[{}], linkName[{}]",
                    entityPath, address, linkName);

                final AmqpChannelProcessor<RequestResponseChannel> requestResponseChannel =
                    createRequestResponseChannel(sessionName, linkName, address);
                return new ManagementChannel(requestResponseChannel, getFullyQualifiedNamespace(), entityPath,
                    tokenManager);
            }));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ClaimsBasedSecurityNode> getClaimsBasedSecurityNode() {
        return connectionMono.then(Mono.fromCallable(() -> getOrCreateCBSNode()));
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
        return connectionMono.map(connection -> {
            final SessionSubscription sessionSubscription = sessionMap.computeIfAbsent(sessionName, key -> {
                final SessionHandler sessionHandler = handlerProvider.createSessionHandler(connectionId,
                    getFullyQualifiedNamespace(), key, connectionOptions.getRetry().getTryTimeout());
                final Session session = connection.session();

                BaseHandler.setHandler(session, sessionHandler);
                final AmqpSession amqpSession = createSession(key, session, sessionHandler);
                final Disposable subscription = amqpSession.getEndpointStates()
                    .subscribe(state -> {
                    }, error -> {
                        // If we were already disposing of the connection, the session would be removed.
                        if (isDisposed.get()) {
                            return;
                        }

                        logger.info("connectionId[{}] sessionName[{}]: Error occurred. Removing and disposing"
                            + " session.", connectionId, sessionName, error);
                        removeSession(key);
                    }, () -> {
                        // If we were already disposing of the connection, the session would be removed.
                        if (isDisposed.get()) {
                            return;
                        }

                        logger.verbose("connectionId[{}] sessionName[{}]: Complete. Removing and disposing session.",
                            connectionId, sessionName);
                        removeSession(key);
                    });

                return new SessionSubscription(amqpSession, subscription);
            });

            return sessionSubscription;
        }).flatMap(sessionSubscription -> {
            final Mono<AmqpEndpointState> activeSession = sessionSubscription.getSession().getEndpointStates()
                .filter(state -> state == AmqpEndpointState.ACTIVE)
                .next()
                .timeout(retryPolicy.getRetryOptions().getTryTimeout(), Mono.error(new AmqpException(true,
                    String.format("connectionId[%s] sessionName[%s] Timeout waiting for session to be active.",
                        connectionId, sessionName), handler.getErrorContext())));

            return activeSession.thenReturn(sessionSubscription.getSession());
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
        return new ReactorSession(this, session, handler, sessionName, reactorProvider,
            handlerProvider, getClaimsBasedSecurityNode(), tokenManagerProvider, messageSerializer,
            connectionOptions.getRetry());
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
        // Because the reactor executor schedules the pending close after the timeout, we want to give sufficient time
        // for the rest of the tasks to run.
        final Duration timeout = operationTimeout.plus(operationTimeout);
        closeAsync().block(timeout);
    }

    /**
     * Gets the active AMQP connection for this instance.
     *
     * @return The AMQP connection.
     *
     * @throws AmqpException if the {@link Connection} was not transitioned to an active state within the given
     *     {@link AmqpRetryOptions#getTryTimeout() operation timeout}.
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
    protected AmqpChannelProcessor<RequestResponseChannel> createRequestResponseChannel(String sessionName,
        String linkName, String entityPath) {

        final Flux<RequestResponseChannel> createChannel = createSession(sessionName)
            .cast(ReactorSession.class)
            .map(reactorSession -> new RequestResponseChannel(this, getId(), getFullyQualifiedNamespace(), linkName,
                entityPath, reactorSession.session(), connectionOptions.getRetry(), handlerProvider, reactorProvider,
                messageSerializer, senderSettleMode, receiverSettleMode))
            .doOnNext(e -> {
                logger.info("connectionId[{}] entityPath[{}] linkName[{}] Emitting new response channel.",
                    getId(), entityPath, linkName);
            })
            .repeat();

        return createChannel
            .subscribeWith(new AmqpChannelProcessor<>(connectionId, entityPath,
                channel -> channel.getEndpointStates(), retryPolicy,
                new ClientLogger(RequestResponseChannel.class.getName() + ":" + entityPath)));
    }

    @Override
    public Mono<Void> closeAsync() {
        if (isDisposed.getAndSet(true)) {
            logger.verbose("connectionId[{}] Was already closed. Not disposing again.", connectionId);
            return isClosedMono.asMono();
        }

        return closeAsync(new AmqpShutdownSignal(false, true,
            "Disposed by client."));
    }

    Mono<Void> closeAsync(AmqpShutdownSignal shutdownSignal) {
        logger.info("connectionId[{}] signal[{}]: Disposing of ReactorConnection.", connectionId, shutdownSignal);

        final Sinks.EmitResult result = shutdownSignalSink.tryEmitValue(shutdownSignal);
        if (result.isFailure()) {
            // It's possible that another one was already emitted, so it's all good.
            logger.info("connectionId[{}] signal[{}] result[{}] Unable to emit shutdown signal.", connectionId, result);
        }

        final Mono<Void> cbsCloseOperation;
        if (cbsChannelProcessor != null) {
            cbsCloseOperation = cbsChannelProcessor.flatMap(channel -> channel.closeAsync());
        } else {
            cbsCloseOperation = Mono.empty();
        }

        final Mono<Void> managementNodeCloseOperations = Mono.when(
            Flux.fromStream(managementNodes.values().stream()).flatMap(node -> node.closeAsync()));

        final Mono<Void> closeReactor = Mono.fromRunnable(() -> {
            final ReactorDispatcher dispatcher = reactorProvider.getReactorDispatcher();

            try {
                if (dispatcher != null) {
                    dispatcher.invoke(this::closeConnectionWork);
                } else {
                    closeConnectionWork();
                }
            } catch (IOException | RejectedExecutionException e) {
                logger.warning("connectionId[{}] Error while scheduling closeConnection work. Manually disposing.",
                    connectionId, e);
                closeConnectionWork();
            }
        });

        return Mono.whenDelayError(cbsCloseOperation, managementNodeCloseOperations)
            .then(closeReactor)
            .then(isClosedMono.asMono());
    }

    private synchronized void closeConnectionWork() {
        if (connection == null) {
            isClosedMono.emitEmpty((signalType, emitResult) -> {
                logger.info("connectionId[{}] signal[{}] result[{}] Unable to complete closeMono.",
                    connectionId, signalType, emitResult);

                return false;
            });

            return;
        }

        connection.close();
        handler.close();

        final ArrayList<Mono<Void>> closingSessions = new ArrayList<>();
        sessionMap.values().forEach(link -> closingSessions.add(link.isClosed()));

        final Mono<Void> closedExecutor = executor != null ? Mono.defer(() ->  {
            synchronized (this) {
                return executor.closeAsync();
            }
        }) : Mono.empty();

        // Close all the children.
        final Mono<Void> closeSessionsMono = Mono.when(closingSessions)
            .timeout(operationTimeout)
            .onErrorResume(error -> {
                logger.warning("connectionId[{}]: Timed out waiting for all sessions to close.", connectionId, error);
                return Mono.empty();
            })
            .then(closedExecutor)
            .then(Mono.fromRunnable(() -> {
                isClosedMono.emitEmpty((signalType, result) -> {
                    logger.warning("connectionId[{}] signal[{}] result[{}]: Unable to emit connection closed signal",
                        connectionId, signalType, result);
                    return false;
                });

                subscriptions.dispose();
            }));

        subscriptions.add(closeSessionsMono.subscribe());
    }

    private synchronized ClaimsBasedSecurityNode getOrCreateCBSNode() {
        if (cbsChannel == null) {
            logger.info("Setting CBS channel.");
            cbsChannelProcessor = createRequestResponseChannel(CBS_SESSION_NAME, CBS_LINK_NAME, CBS_ADDRESS);
            cbsChannel = new ClaimsBasedSecurityChannel(
                cbsChannelProcessor,
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

            final ReactorExceptionHandler reactorExceptionHandler = new ReactorExceptionHandler();

            // Use a new single-threaded scheduler for this connection as QPID's Reactor is not thread-safe.
            // Using Schedulers.single() will use the same thread for all connections in this process which
            // limits the scalability of the no. of concurrent connections a single process can have.
            // This could be a long timeout depending on the user's operation timeout. It's probable that the
            // connection's long disposed.
            final Duration timeoutDivided = connectionOptions.getRetry().getTryTimeout().dividedBy(2);
            final Duration pendingTasksDuration = ClientConstants.SERVER_BUSY_WAIT_TIME.compareTo(timeoutDivided) < 0
                ? ClientConstants.SERVER_BUSY_WAIT_TIME
                : timeoutDivided;
            final Scheduler scheduler = Schedulers.newSingle("reactor-executor");
            executor = new ReactorExecutor(reactor, scheduler, connectionId,
                reactorExceptionHandler, pendingTasksDuration,
                connectionOptions.getFullyQualifiedNamespace());

            // To avoid inconsistent synchronization of executor, we set this field with the closeAsync method.
            // It will not be kicked off until subscribed to.
            final Mono<Void> executorCloseMono = Mono.defer(() ->  {
                synchronized (this) {
                    return executor.closeAsync();
                }
            });
            reactorProvider.getReactorDispatcher().getShutdownSignal()
                .flatMap(signal -> {
                    logger.info("Shutdown signal received from reactor provider.");
                    reactorExceptionHandler.onConnectionShutdown(signal);
                    return executorCloseMono;
                })
                .onErrorResume(error -> {
                    logger.info("Error received from reactor provider.", error);
                    reactorExceptionHandler.onConnectionError(error);
                    return executorCloseMono;
                })
                .subscribe();

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
            logger.info(
                "onConnectionError connectionId[{}], hostName[{}], message[Starting new reactor], error[{}]",
                getId(), getFullyQualifiedNamespace(), exception.getMessage(), exception);

            if (!isDisposed.getAndSet(true)) {
                logger.verbose("onReactorError connectionId[{}], hostName[{}]: Disposing.", connectionId,
                    getFullyQualifiedNamespace());
                closeAsync(new AmqpShutdownSignal(false, false,
                    "onReactorError: " + exception.toString()))
                    .subscribe();
            }
        }

        @Override
        void onConnectionShutdown(AmqpShutdownSignal shutdownSignal) {
            logger.info(
                "onConnectionShutdown connectionId[{}], hostName[{}], message[Shutting down], shutdown signal[{}]",
                getId(), getFullyQualifiedNamespace(), shutdownSignal.isInitiatedByClient(), shutdownSignal);

            if (!isDisposed.getAndSet(true)) {
                logger.verbose("onConnectionShutdown connectionId[{}], hostName[{}]: disposing.");
                closeAsync(shutdownSignal).subscribe();
            }
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

        private AmqpSession getSession() {
            return session;
        }

        private void dispose() {
            if (isDisposed.getAndSet(true)) {
                return;
            }

            if (session instanceof ReactorSession) {
                ((ReactorSession) session).closeAsync("Closing session.", null, true)
                    .subscribe();
            } else {
                session.dispose();
            }

            subscription.dispose();
        }

        private Mono<Void> isClosed() {
            if (session instanceof ReactorSession) {
                return ((ReactorSession) session).isClosed();
            } else {
                return Mono.empty();
            }
        }
    }
}
