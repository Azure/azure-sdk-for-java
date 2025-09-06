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

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.amqp.exception.AmqpErrorCondition.TIMEOUT_ERROR;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addShutdownSignal;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addSignalTypeAndResult;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.createContextWithConnectionId;
import static com.azure.core.amqp.implementation.ClientConstants.EMIT_RESULT_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.HOSTNAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.SESSION_NAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.SIGNAL_TYPE_KEY;
import static com.azure.core.util.FluxUtil.monoError;

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

    private final ClientLogger logger;
    private final ReactorSessionCache sessionCache;
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
    private final AmqpLinkProvider linkProvider;
    private final TokenManagerProvider tokenManagerProvider;
    private final MessageSerializer messageSerializer;
    private final ConnectionOptions connectionOptions;
    private final ReactorProvider reactorProvider;
    private final AmqpRetryPolicy retryPolicy;
    private final SenderSettleMode senderSettleMode;
    private final ReceiverSettleMode receiverSettleMode;
    private final Duration operationTimeout;
    private final Composite subscriptions;

    private ReactorExecutor reactorExecutor;

    private volatile ClaimsBasedSecurityChannel cbsChannel;
    private volatile AmqpChannelProcessor<RequestResponseChannel> cbsChannelProcessor;
    private volatile RequestResponseChannelCache cbsChannelCache;
    private volatile Connection connection;
    private final boolean isV2;
    private final boolean useSessionChannelCache;

    /**
     * Creates a new AMQP connection that uses proton-j.
     *
     * @param connectionId Identifier for the connection.
     * @param connectionOptions A set of options used to create the AMQP connection.
     * @param reactorProvider Provides proton-j Reactor instances.
     * @param handlerProvider Provides {@link BaseHandler} to listen to proton-j reactor events.
     * @param linkProvider Provides amqp links for send and receive.
     * @param tokenManagerProvider Provides the appropriate token manager to authorize with CBS node.
     * @param messageSerializer Serializer to translate objects to and from proton-j {@link Message messages}.
     * @param senderSettleMode to set as {@link SenderSettleMode} on sender.
     * @param receiverSettleMode to set as {@link ReceiverSettleMode} on receiver.
     * @param isV2 (temporary) flag to use either v1 or v2 stack.
     * @param useSessionChannelCache indicates if {@link ReactorSessionCache} and {@link RequestResponseChannelCache}
     *     should be used when in v2 mode.
     */
    public ReactorConnection(String connectionId, ConnectionOptions connectionOptions, ReactorProvider reactorProvider,
        ReactorHandlerProvider handlerProvider, AmqpLinkProvider linkProvider,
        TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer,
        SenderSettleMode senderSettleMode, ReceiverSettleMode receiverSettleMode, boolean isV2,
        boolean useSessionChannelCache) {

        this.connectionOptions = connectionOptions;
        this.reactorProvider = reactorProvider;
        this.connectionId = connectionId;
        this.logger = new ClientLogger(ReactorConnection.class, createContextWithConnectionId(connectionId));
        this.handlerProvider = handlerProvider;
        this.linkProvider = linkProvider;
        this.tokenManagerProvider
            = Objects.requireNonNull(tokenManagerProvider, "'tokenManagerProvider' cannot be null.");
        this.messageSerializer = messageSerializer;
        this.handler = handlerProvider.createConnectionHandler(connectionId, connectionOptions);

        this.retryPolicy = RetryUtil.getRetryPolicy(connectionOptions.getRetry());
        this.operationTimeout = connectionOptions.getRetry().getTryTimeout();
        this.senderSettleMode = senderSettleMode;
        this.receiverSettleMode = receiverSettleMode;
        this.isV2 = isV2;
        if (!isV2 && useSessionChannelCache) {
            // Internal-Error: When client is in V1 mode, the builder should have ignored the attempt to opt in
            // the "com.azure.core.amqp.cache" configuration.
            logger.atError().log("Internal-Error: Unexpected attempt to use SessionCache and ChannelCache in V1.");
            this.useSessionChannelCache = false;
        } else {
            this.useSessionChannelCache = useSessionChannelCache;
        }
        this.sessionCache = new ReactorSessionCache(connectionId, handler.getHostname(), handlerProvider,
            reactorProvider, operationTimeout, logger);

        this.connectionMono = Mono.fromCallable(this::getOrCreateConnection).flatMap(reactorConnection -> {
            final Mono<AmqpEndpointState> activeEndpoint
                = getEndpointStates().filter(state -> state == AmqpEndpointState.ACTIVE)
                    .next()
                    .timeout(operationTimeout, Mono.error(() -> {
                        AmqpException exception = new AmqpException(true, TIMEOUT_ERROR,
                            String.format("Connection '%s' not active within the timeout: %s.", connectionId,
                                operationTimeout),
                            handler.getErrorContext());
                        if (!isV2) {
                            // this is temp patch to make v1 stack retry on timeout.
                            // V2 stack does connection management differently via ReactorConnectionCache
                            // and does not need to call into handler here
                            handler.onError(exception);
                        }
                        return exception;
                    }));
            return activeEndpoint.thenReturn(reactorConnection);
        }).doOnError(error -> {
            if (setDisposed()) {
                closeAsync(new AmqpShutdownSignal(false, false,
                    "Error occurred while connection was starting. Error: " + error)).subscribe();
            } else {
                logger.verbose("Connection was already disposed: Error occurred while connection was starting.", error);
            }
        });

        this.endpointStates
            = this.handler.getEndpointStates().takeUntilOther(shutdownSignalSink.asMono()).map(state -> {
                logger.atVerbose().addKeyValue("state", state).log("getConnectionState");
                return AmqpEndpointStateUtil.getConnectionState(state);
            }).onErrorResume(error -> {
                if (setDisposed()) {
                    logger.verbose("Disposing of active sessions due to error.");
                    return closeAsync(new AmqpShutdownSignal(false, false, error.getMessage())).then(Mono.error(error));
                } else {
                    return Mono.error(error);
                }
            }).doOnComplete(() -> {
                if (setDisposed()) {
                    logger.verbose("Disposing of active sessions due to connection close.");
                    closeAsync(new AmqpShutdownSignal(false, false, "Connection handler closed.")).subscribe();
                }
            }).cache(1);

        this.subscriptions = Disposables.composite(
            this.endpointStates.subscribe(null, e -> logger.warning("Connection endpoint state signaled error .", e)));
    }

    /**
     * Establish a connection to the broker and wait for it to active.
     *
     * @return the {@link Mono} that completes once the broker connection is established and active.
     */
    public Mono<ReactorConnection> connectAndAwaitToActive() {
        return this.connectionMono.handle((c, sink) -> {
            if (isDisposed()) {
                // Today 'connectionMono' emits QPID-connection even if the endpoint terminated with
                // 'completion' without ever emitting any state. (Had it emitted a state and never
                // emitted 'active', then timeout-error would have happened, then 'handle' won't be
                // running, same with endpoint terminating with any error).
                sink.error(new AmqpException(true,
                    String.format("Connection '%s' completed without being active.", connectionId), null));
            } else {
                sink.complete();
            }
        }).thenReturn(this);
    }

    public void transferState(ReactorConnection fromConnection) {
        if (fromConnection == null) {
            return;
        }
        this.handler.transferState(fromConnection.handler);
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
                return monoError(logger.atWarning().addKeyValue(ENTITY_PATH_KEY, entityPath),
                    new IllegalStateException("Connection is disposed. Cannot get management instance."));
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

                logger.atInfo()
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .addKeyValue(LINK_NAME_KEY, linkName)
                    .addKeyValue("address", address)
                    .log("Creating management node.");

                final ChannelCacheWrapper channelCache;
                if (useSessionChannelCache) {
                    // V2 with 'SessionCache,RequestResponseChannelCache' opted-in.
                    final RequestResponseChannelCache cache
                        = new RequestResponseChannelCache(this, sessionName, linkName, address, retryPolicy);
                    channelCache = new ChannelCacheWrapper(cache);
                } else {
                    // V2 without 'SessionCache,RequestResponseChannelCache' opt-in or V1.
                    final AmqpChannelProcessor<RequestResponseChannel> cache
                        = createRequestResponseChannel(sessionName, linkName, address);
                    channelCache = new ChannelCacheWrapper(cache);
                }
                return new ManagementChannel(channelCache, getFullyQualifiedNamespace(), entityPath, tokenManager);
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
        if (useSessionChannelCache) {
            // V2 with 'SessionCache,RequestResponseChannelCache' opted-in.
            final ReactorSessionCache.Loader loader = this::createSession;
            return sessionCache.getOrLoad(connectionMono, sessionName, loader).cast(AmqpSession.class);
        }
        // V2 without 'SessionCache,RequestResponseChannelCache' opt-in or V1.
        return connectionMono.map(connection -> {
            return sessionMap.computeIfAbsent(sessionName, key -> {
                final SessionHandler sessionHandler = handlerProvider.createSessionHandler(connectionId,
                    getFullyQualifiedNamespace(), key, connectionOptions.getRetry().getTryTimeout());
                final Session session = connection.session();

                BaseHandler.setHandler(session, sessionHandler);
                final ProtonSessionWrapper sessionWrapper
                    = new ProtonSessionWrapper(session, sessionHandler, reactorProvider);
                final AmqpSession amqpSession = createSession(sessionWrapper);
                final Disposable subscription = amqpSession.getEndpointStates().subscribe(state -> {
                }, error -> {
                    // If we were already disposing of the connection, the session would be removed.
                    if (isDisposed.get()) {
                        return;
                    }

                    logger.atInfo()
                        .addKeyValue(SESSION_NAME_KEY, sessionName)
                        .log("Error occurred. Removing and disposing session", error);
                    removeSession(key);
                }, () -> {
                    // If we were already disposing of the connection, the session would be removed.
                    if (isDisposed.get()) {
                        return;
                    }

                    logger.atVerbose()
                        .addKeyValue(SESSION_NAME_KEY, sessionName)
                        .log("Complete. Removing and disposing session.");
                    removeSession(key);
                });

                return new SessionSubscription(amqpSession, subscription);
            });
        }).flatMap(sessionSubscription -> {
            final Mono<AmqpEndpointState> activeSession = sessionSubscription.getSession()
                .getEndpointStates()
                .filter(state -> state == AmqpEndpointState.ACTIVE)
                .next()
                .timeout(retryPolicy.getRetryOptions().getTryTimeout(),
                    Mono.error(() -> new AmqpException(true, TIMEOUT_ERROR,
                        String.format("connectionId[%s] sessionName[%s] Timeout waiting for session to be active.",
                            connectionId, sessionName),
                        handler.getErrorContext())))
                .doOnError(error -> {
                    // Clean up the subscription if there was an error waiting for the session to become active.

                    if (!(error instanceof AmqpException)) {
                        return;
                    }

                    final AmqpException amqpException = (AmqpException) error;
                    if (amqpException.getErrorCondition() == TIMEOUT_ERROR) {
                        final SessionSubscription removed = sessionMap.remove(sessionName);
                        removed.dispose();
                    }
                });

            return activeSession.thenReturn(sessionSubscription.getSession());
        });
    }

    /**
     * Creates a new ReactorSession that uses the given low-level session.
     * <p>
     * TODO (anu): Use 'ProtonSession' as the arg when removing v1 and 'SessionCache' (hence 'ProtonSession') is no
     *  longer opt-in for v2.
     * </p>
     * @param session the QPid Proton-j session.
     *
     * @return A new instance of ReactorSession.
     */
    protected ReactorSession createSession(ProtonSessionWrapper session) {
        return new ReactorSession(this, session, handlerProvider, linkProvider, getClaimsBasedSecurityNode(),
            tokenManagerProvider, messageSerializer, connectionOptions.getRetry());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeSession(String sessionName) {
        if (useSessionChannelCache) {
            // V2 with 'SessionCache,RequestResponseChannelCache' opted-in.
            return sessionCache.evict(sessionName);
        }

        // V2 without 'SessionCache,RequestResponseChannelCache' opt-in or V1.
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
     * <p>
     *  TODO (anu): this method will be removed when dropping the v1 support. The libraries completely on
     *   v2 stack will use {@link #newRequestResponseChannel(String, String, String)} API instead.
     * </p>
     * @param sessionName Name of the session.
     * @param linkName Name of the link.
     * @param entityPath Address to the message broker.
     *
     * @return A new {@link RequestResponseChannel} to communicate with the message broker.
     */
    protected AmqpChannelProcessor<RequestResponseChannel> createRequestResponseChannel(String sessionName,
        String linkName, String entityPath) {
        assert !isV2 || !useSessionChannelCache;
        Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");

        final Flux<RequestResponseChannel> createChannel = createSession(sessionName).cast(ReactorSession.class)
            .flatMap(reactorSession -> reactorSession.channel(linkName))
            .map(channel -> new RequestResponseChannel(this, getId(), getFullyQualifiedNamespace(), entityPath, channel,
                connectionOptions.getRetry(), handlerProvider, reactorProvider, messageSerializer, senderSettleMode,
                receiverSettleMode, handlerProvider.getMetricProvider(getFullyQualifiedNamespace(), entityPath), isV2))
            .doOnNext(e -> {
                logger.atInfo()
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .addKeyValue(LINK_NAME_KEY, linkName)
                    .log("Emitting new response channel.");
            })
            // Create channel only when connection is active, to avoid repeatedly requesting and closing channels
            // after connection emits the shutdown signal.
            .repeat(() -> !this.isDisposed());

        Map<String, Object> loggingContext = createContextWithConnectionId(connectionId);
        loggingContext.put(ENTITY_PATH_KEY, entityPath);

        return createChannel.subscribeWith(new AmqpChannelProcessor<>(getFullyQualifiedNamespace(),
            channel -> channel.getEndpointStates(), retryPolicy, loggingContext));
    }

    /**
     * Creates a bidirectional channel between the message broker and the client.
     *
     * @param sessionName the AMQP session to host the channel.
     * @param name the channel name.
     * @param entityPath the address to the message broker.
     *
     * @return A new {@link RequestResponseChannel} to communicate with the message broker.
     */
    protected Mono<RequestResponseChannel> newRequestResponseChannel(String sessionName, String name,
        String entityPath) {
        assert isV2 && useSessionChannelCache;
        Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");

        return createSession(sessionName).cast(ReactorSession.class)
            .flatMap(reactorSession -> reactorSession.channel(name))
            .map(channel -> new RequestResponseChannel(this, getId(), getFullyQualifiedNamespace(), entityPath, channel,
                connectionOptions.getRetry(), handlerProvider, reactorProvider, messageSerializer, senderSettleMode,
                receiverSettleMode, handlerProvider.getMetricProvider(getFullyQualifiedNamespace(), entityPath), isV2));
    }

    @Override
    public Mono<Void> closeAsync() {
        if (setDisposed()) {
            return closeAsync(new AmqpShutdownSignal(false, true, "Disposed by client."));
        } else {
            logger.verbose("Connection was already closed. Not disposing again.");
            return isClosedMono.asMono();
        }
    }

    /**
     * Disposes of the connection.
     *
     * @param shutdownSignal Shutdown signal to emit.
     * @return A mono that completes when the connection is disposed.
     */
    private Mono<Void> closeAsync(AmqpShutdownSignal shutdownSignal) {
        addShutdownSignal(logger.atInfo(), shutdownSignal).log("Disposing of ReactorConnection.");
        final Sinks.EmitResult result = shutdownSignalSink.tryEmitValue(shutdownSignal);

        if (result.isFailure()) {
            // It's possible that another one was already emitted, so it's all good.
            addShutdownSignal(logger.atInfo(), shutdownSignal).addKeyValue(EMIT_RESULT_KEY, result)
                .log("Unable to emit shutdown signal.");
        }

        final Mono<Void> cbsCloseOperation;
        if (useSessionChannelCache) {
            // V2 with 'SessionCache,RequestResponseChannelCache' opted-in.
            if (cbsChannelCache != null) {
                cbsCloseOperation = cbsChannelCache.closeAsync();
            } else {
                cbsCloseOperation = Mono.empty();
            }
        } else {
            // V2 without 'SessionCache,RequestResponseChannelCache' opt-in or V1.
            if (cbsChannelProcessor != null) {
                cbsCloseOperation = cbsChannelProcessor.flatMap(channel -> channel.closeAsync());
            } else {
                cbsCloseOperation = Mono.empty();
            }
        }

        final Mono<Void> managementNodeCloseOperations
            = Mono.when(Flux.fromStream(managementNodes.values().stream()).flatMap(node -> node.closeAsync()));

        final Mono<Void> closeReactor = Mono.fromRunnable(() -> {
            logger.verbose("Scheduling closeConnection work.");
            final ReactorDispatcher dispatcher = reactorProvider.getReactorDispatcher();

            if (dispatcher != null) {
                try {
                    dispatcher.invoke(() -> closeConnectionWork());
                } catch (IOException e) {
                    logger.warning("IOException while scheduling closeConnection work. Manually disposing.", e);

                    closeConnectionWork();
                } catch (RejectedExecutionException e) {
                    // Not logging error here again because we have to log the exception when we throw it.
                    logger.info("Could not schedule closeConnection work. Manually disposing.");

                    closeConnectionWork();
                }
            } else {
                closeConnectionWork();
            }
        });

        return Mono
            .whenDelayError(
                cbsCloseOperation.doFinally(
                    signalType -> logger.atVerbose().addKeyValue(SIGNAL_TYPE_KEY, signalType).log("Closed CBS node.")),
                managementNodeCloseOperations.doFinally(signalType -> logger.atVerbose()
                    .addKeyValue(SIGNAL_TYPE_KEY, signalType)
                    .log("Closed management nodes.")))
            .then(closeReactor.doFinally(signalType -> logger.atVerbose()
                .addKeyValue(SIGNAL_TYPE_KEY, signalType)
                .log("Closed reactor dispatcher.")))
            .then(isClosedMono.asMono());
    }

    private synchronized void closeConnectionWork() {
        if (connection == null) {
            isClosedMono.emitEmpty((signalType, emitResult) -> {
                addSignalTypeAndResult(logger.atInfo(), signalType, emitResult).log("Unable to complete closeMono.");

                return false;
            });

            return;
        }

        connection.close();
        handler.close();

        final Mono<Void> awaitSessionsClose;
        if (useSessionChannelCache) {
            // V2 with 'SessionCache,RequestResponseChannelCache' opted-in.
            awaitSessionsClose = sessionCache.awaitClose();
        } else {
            // V2 without 'SessionCache,RequestResponseChannelCache' opt-in or V1.
            final ArrayList<Mono<Void>> closingSessions = new ArrayList<>();
            sessionMap.values().forEach(link -> closingSessions.add(link.isClosed()));
            awaitSessionsClose = Mono.when(closingSessions);
        }

        // We shouldn't need to add a timeout to this operation because executorCloseMono schedules its last
        // remaining work after OperationTimeout has elapsed and closes afterwards.
        final Mono<Void> closedExecutor = reactorExecutor != null ? Mono.defer(() -> {
            synchronized (this) {
                logger.info("Closing executor.");
                return reactorExecutor.closeAsync();
            }
        }) : Mono.empty();

        // Close all the children and the ReactorExecutor.
        final Mono<Void> closeSessionAndExecutorMono
            = awaitSessionsClose.timeout(operationTimeout).onErrorResume(error -> {
                logger.info("Timed out waiting for all sessions to close.");
                return Mono.empty();
            }).then(closedExecutor).then(Mono.fromRunnable(() -> {
                isClosedMono.emitEmpty((signalType, result) -> {
                    addSignalTypeAndResult(logger.atWarning(), signalType, result)
                        .log("Unable to emit connection closed signal.");
                    return false;
                });

                subscriptions.dispose();
            }));

        subscriptions.add(closeSessionAndExecutorMono.subscribe());
    }

    private synchronized ClaimsBasedSecurityNode getOrCreateCBSNode() {
        if (cbsChannel == null) {
            logger.info("Setting CBS channel.");
            if (useSessionChannelCache) {
                // V2 with 'SessionCache,RequestResponseChannelCache' opted-in.
                cbsChannelCache
                    = new RequestResponseChannelCache(this, CBS_ADDRESS, CBS_SESSION_NAME, CBS_LINK_NAME, retryPolicy);
                cbsChannel
                    = new ClaimsBasedSecurityChannel(cbsChannelCache.get(), connectionOptions.getTokenCredential(),
                        connectionOptions.getAuthorizationType(), connectionOptions.getRetry());
            } else {
                // V2 without 'SessionCache,RequestResponseChannelCache' opt-in or V1.
                cbsChannelProcessor = createRequestResponseChannel(CBS_SESSION_NAME, CBS_LINK_NAME, CBS_ADDRESS);
                cbsChannel = new ClaimsBasedSecurityChannel(cbsChannelProcessor, connectionOptions.getTokenCredential(),
                    connectionOptions.getAuthorizationType(), connectionOptions.getRetry());
            }
        }
        return cbsChannel;
    }

    private synchronized Connection getOrCreateConnection() throws IOException {
        if (connection == null) {
            logger.atInfo()
                .addKeyValue(HOSTNAME_KEY, handler.getHostname())
                .addKeyValue("port", handler.getProtocolPort())
                .log("Creating and starting connection.");

            final Reactor reactor = reactorProvider.createReactor(connectionId, handler.getMaxFrameSize());
            connection = reactor.connectionToHost(handler.getHostname(), handler.getProtocolPort(), handler);

            final ReactorExceptionHandler reactorExceptionHandler = new ReactorExceptionHandler();

            reactorExecutor = reactorProvider.createExecutor(reactor, connectionId,
                connectionOptions.getFullyQualifiedNamespace(), reactorExceptionHandler, connectionOptions.getRetry());

            // To avoid inconsistent synchronization of executor, we set this field with the closeAsync method.
            // It will not be kicked off until subscribed to.
            final Mono<Void> executorCloseMono = Mono.defer(() -> {
                synchronized (this) {
                    return reactorExecutor.closeAsync();
                }
            });

            // We shouldn't need to add a timeout to this operation because executorCloseMono schedules its last
            // remaining work after OperationTimeout has elapsed and closes afterwards.
            reactorProvider.getReactorDispatcher().getShutdownSignal().flatMap(signal -> {
                reactorExceptionHandler.onConnectionShutdown(signal);
                return executorCloseMono;
            }).onErrorResume(error -> {
                reactorExceptionHandler.onConnectionError(error);
                return executorCloseMono;
            }).subscribe();

            reactorExecutor.start();
        }

        return connection;
    }

    /**
     * Sets the atomic flag indicating that this connection is disposed of.
     *
     * @return true if the flag is set for the first time, false if it was already set.
     */
    private boolean setDisposed() {
        final boolean firstDisposal = !isDisposed.getAndSet(true);
        if (firstDisposal) {
            sessionCache.setOwnerDisposed();
            return true;
        } else {
            return false;
        }
    }

    /**
     * ReactorExceptionHandler handles exceptions that occur in the reactor.
     */
    public final class ReactorExceptionHandler extends AmqpExceptionHandler {
        private ReactorExceptionHandler() {
            super();
        }

        @Override
        public void onConnectionError(Throwable exception) {
            logger.atInfo()
                .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, getFullyQualifiedNamespace())
                .log("onConnectionError, Starting new reactor", exception);

            if (setDisposed()) {
                logger.atVerbose()
                    .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, getFullyQualifiedNamespace())
                    .log("onReactorError: Disposing.");

                closeAsync(new AmqpShutdownSignal(false, false, "onReactorError: " + exception.toString())).subscribe();
            }
        }

        @Override
        void onConnectionShutdown(AmqpShutdownSignal shutdownSignal) {
            addShutdownSignal(logger.atInfo(), shutdownSignal)
                .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, getFullyQualifiedNamespace())
                .log("onConnectionShutdown. Shutting down.");

            if (setDisposed()) {
                logger.atVerbose()
                    .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, getFullyQualifiedNamespace())
                    .log("onConnectionShutdown: disposing.");

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
                ((ReactorSession) session).closeAsync("Closing session.", null, true).subscribe();
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
