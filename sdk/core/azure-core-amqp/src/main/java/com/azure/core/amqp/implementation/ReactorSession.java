// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.amqp.AmqpTransactionCoordinator;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transaction.Coordinator;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.core.amqp.implementation.AmqpConstants.CLIENT_IDENTIFIER;
import static com.azure.core.amqp.implementation.AmqpConstants.CLIENT_RECEIVER_IDENTIFIER;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addErrorCondition;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addSignalTypeAndResult;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.createContextWithConnectionId;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;
import static com.azure.core.amqp.implementation.ClientConstants.SESSION_NAME_KEY;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * Represents an AMQP session using proton-j reactor.
 */
public class ReactorSession implements AmqpSession {
    private static final String TRANSACTION_LINK_NAME = "coordinator";
    private final ConcurrentMap<String, LinkSubscription<AmqpSendLink>> openSendLinks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LinkSubscription<AmqpReceiveLink>> openReceiveLinks = new ConcurrentHashMap<>();

    private final Scheduler timeoutScheduler = Schedulers.parallel();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Object closeLock = new Object();

    /**
     * Mono that completes when the session is completely closed, that is that the session remote
     */
    private final Sinks.Empty<Void> isClosedMono = Sinks.empty();

    private final ClientLogger logger;
    private final Flux<AmqpEndpointState> endpointStates;

    private final AmqpConnection amqpConnection;
    private final Session session;
    private final SessionHandler sessionHandler;
    private final String sessionName;
    private final ReactorProvider provider;
    private final TokenManagerProvider tokenManagerProvider;
    private final MessageSerializer messageSerializer;
    private final String activeTimeoutMessage;
    private final AmqpRetryOptions retryOptions;

    private final ReactorHandlerProvider handlerProvider;
    private final AmqpLinkProvider linkProvider;
    private final Mono<ClaimsBasedSecurityNode> cbsNodeSupplier;
    private final Disposable.Composite subscriptions = Disposables.composite();

    private final AtomicReference<TransactionCoordinator> transactionCoordinator = new AtomicReference<>();
    private final Flux<AmqpShutdownSignal> shutdownSignals;

    /**
     * Creates a new AMQP session using proton-j.
     *
     * @param amqpConnection AMQP connection associated with this session.
     * @param session Proton-j session for this AMQP session.
     * @param sessionHandler Handler for events that occur in the session.
     * @param sessionName Name of the session.
     * @param provider Provides reactor instances for messages to sent with.
     * @param handlerProvider Providers reactor handlers for listening to proton-j reactor events.
     * @param linkProvider Provides AMQP links that are created from proton-j links.
     * @param cbsNodeSupplier Mono that returns a reference to the {@link ClaimsBasedSecurityNode}.
     * @param tokenManagerProvider Provides {@link TokenManager} that authorizes the client when performing
     *     operations on the message broker.
     * @param messageSerializer Serializes and deserializes proton-j messages.
     * @param retryOptions for the session operations.
     */
    public ReactorSession(AmqpConnection amqpConnection, Session session, SessionHandler sessionHandler,
        String sessionName, ReactorProvider provider, ReactorHandlerProvider handlerProvider,
        AmqpLinkProvider linkProvider, Mono<ClaimsBasedSecurityNode> cbsNodeSupplier,
        TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer, AmqpRetryOptions retryOptions) {
        this.amqpConnection = amqpConnection;
        this.session = session;
        this.sessionHandler = sessionHandler;
        this.handlerProvider = handlerProvider;
        this.sessionName = sessionName;
        this.provider = provider;
        this.linkProvider = linkProvider;
        this.cbsNodeSupplier = cbsNodeSupplier;
        this.tokenManagerProvider = tokenManagerProvider;
        this.messageSerializer = messageSerializer;
        this.retryOptions = retryOptions;
        this.activeTimeoutMessage = String.format(
            "ReactorSession connectionId[%s], session[%s]: Retries exhausted waiting for ACTIVE endpoint state.",
            sessionHandler.getConnectionId(), sessionName);

        this.logger = new ClientLogger(ReactorSession.class,
            createContextWithConnectionId(this.sessionHandler.getConnectionId()));

        this.endpointStates = sessionHandler.getEndpointStates().map(state -> {
            logger.atVerbose()
                .addKeyValue(SESSION_NAME_KEY, sessionName)
                .addKeyValue("state", state)
                .log("Got endpoint state.");

            return AmqpEndpointStateUtil.getConnectionState(state);
        }).doOnError(error -> handleError(error)).doOnComplete(() -> handleClose()).cache(1);

        shutdownSignals = amqpConnection.getShutdownSignals();
        subscriptions.add(this.endpointStates.subscribe());
        subscriptions.add(shutdownSignals
            .flatMap(signal -> closeAsync("Shutdown signal received (" + signal.toString() + ")", null, false))
            .subscribe());

        session.open();
    }

    Session session() {
        return this.session;
    }

    @Override
    public Flux<AmqpEndpointState> getEndpointStates() {
        return endpointStates;
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
        closeAsync().block(retryOptions.getTryTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSessionName() {
        return sessionName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getOperationTimeout() {
        return retryOptions.getTryTimeout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpTransaction> createTransaction() {
        return getOrCreateTransactionCoordinator().flatMap(coordinator -> coordinator.declare());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> commitTransaction(AmqpTransaction transaction) {
        return getOrCreateTransactionCoordinator().flatMap(coordinator -> coordinator.discharge(transaction, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> rollbackTransaction(AmqpTransaction transaction) {
        return getOrCreateTransactionCoordinator().flatMap(coordinator -> coordinator.discharge(transaction, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpLink> createProducer(String linkName, String entityPath, Duration timeout, AmqpRetryPolicy retry) {
        return createProducer(linkName, entityPath, timeout, retry, null)
            .or(onClosedError("Connection closed while waiting for new producer link.", entityPath, linkName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpLink> createConsumer(String linkName, String entityPath, Duration timeout, AmqpRetryPolicy retry) {
        // Note: As part of removing the v1 stack receiver, the 'createConsumer' invoked below will be updated by
        // removing
        // ConsumerFactory parameter and adding two additional parameters (DeliverySettleMode,
        // includeDeliveryTagInMessage).
        // Here we've to pass (DeliverySettleMode.SETTLE_ON_DELIVERY, false) as the values for those two parameters.
        return createConsumer(linkName, entityPath, timeout, retry, null, null, null, SenderSettleMode.UNSETTLED,
            ReceiverSettleMode.SECOND, new ConsumerFactory())
                .or(onClosedError("Connection closed while waiting for new receive link.", entityPath, linkName))
                .cast(AmqpLink.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeLink(String linkName) {
        return removeLink(openSendLinks, linkName) || removeLink(openReceiveLinks, linkName);
    }

    /**
     * A Mono that completes when the session has completely closed.
     *
     * @return Mono that completes when the session has completely closed.
     */
    Mono<Void> isClosed() {
        return isClosedMono.asMono();
    }

    @Override
    public Mono<Void> closeAsync() {
        return closeAsync(null, null, true);
    }

    Mono<Void> closeAsync(String message, ErrorCondition errorCondition, boolean disposeLinks) {
        if (isDisposed.getAndSet(true)) {
            return isClosedMono.asMono();
        }

        addErrorCondition(logger.atVerbose(), errorCondition).addKeyValue(SESSION_NAME_KEY, sessionName)
            .log("Setting error condition and disposing session. {}", message);

        return Mono.fromRunnable(() -> {
            try {
                provider.getReactorDispatcher().invoke(() -> disposeWork(errorCondition, disposeLinks));
            } catch (IOException e) {
                logger.atInfo()
                    .addKeyValue(SESSION_NAME_KEY, sessionName)
                    .log("Error while scheduling work. Manually disposing.", e);

                disposeWork(errorCondition, disposeLinks);
            } catch (RejectedExecutionException e) {
                logger.atInfo()
                    .addKeyValue(SESSION_NAME_KEY, sessionName)
                    .log("RejectedExecutionException when scheduling work.");

                disposeWork(errorCondition, disposeLinks);
            }
        }).then(isClosedMono.asMono());
    }

    /**
     * @return {@link Mono} of {@link TransactionCoordinator}
     */
    @Override
    public Mono<? extends AmqpTransactionCoordinator> getOrCreateTransactionCoordinator() {
        if (isDisposed()) {
            return monoError(logger.atWarning().addKeyValue(SESSION_NAME_KEY, sessionName), new AmqpException(true,
                String.format("Cannot create coordinator send link %s from a closed session.", TRANSACTION_LINK_NAME),
                sessionHandler.getErrorContext()));
        }

        final TransactionCoordinator existing = transactionCoordinator.get();
        if (existing != null) {
            logger.atVerbose()
                .addKeyValue("coordinator", TRANSACTION_LINK_NAME)
                .log("Returning existing transaction coordinator.");
            return Mono.just(existing);
        }

        return createProducer(TRANSACTION_LINK_NAME, TRANSACTION_LINK_NAME, new Coordinator(), retryOptions, null,
            false).map(link -> {
                final TransactionCoordinator newCoordinator = new TransactionCoordinator(link, messageSerializer);
                if (transactionCoordinator.compareAndSet(null, newCoordinator)) {
                    return newCoordinator;
                } else {
                    return transactionCoordinator.get();
                }
            })
                .or(onClosedError("Connection closed while waiting for transaction coordinator creation.",
                    NOT_APPLICABLE, NOT_APPLICABLE));
    }

    /**
     * Creates an {@link AmqpReceiveLink} that has AMQP specific capabilities set.
     * <p>
     * Filters can be applied to the source when receiving to inform the source to filter the items sent to the
     * consumer. See
     * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#doc-idp326640">Filtering
     * Messages</a> and <a href="https://www.amqp.org/specification/1.0/filters">AMQP Filters</a> for more information.
     *
     * @param linkName Name of the receive link.
     * @param entityPath Address in the message broker for the link.
     * @param timeout Operation timeout when creating the link.
     * @param retry Retry policy to apply when link creation times out.
     * @param sourceFilters Add any filters to the source when creating the receive link.
     * @param receiverProperties Any properties to associate with the receive link when attaching to message
     *     broker.
     * @param receiverDesiredCapabilities Capabilities that the receiver link supports.
     * @param senderSettleMode Amqp {@link SenderSettleMode} mode for receiver.
     * @param receiverSettleMode Amqp {@link ReceiverSettleMode} mode for receiver.
     * @param consumerFactory a temporary parameter to support both v1 and v2 receivers. When removing the v1
     *       receiver support, two new parameters, 'ReceiverSettleMode' and 'includeDeliveryTagInMessage' will be introduced,
     *       and 'consumerFactory' will be removed.
     * @return A new instance of an {@link AmqpReceiveLink} with the correct properties set.
     */
    protected Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, Duration timeout,
        AmqpRetryPolicy retry, Map<Symbol, Object> sourceFilters, Map<Symbol, Object> receiverProperties,
        Symbol[] receiverDesiredCapabilities, SenderSettleMode senderSettleMode, ReceiverSettleMode receiverSettleMode,
        ConsumerFactory consumerFactory) {

        if (isDisposed()) {
            LoggingEventBuilder logBuilder = logger.atWarning()
                .addKeyValue(SESSION_NAME_KEY, sessionName)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue(LINK_NAME_KEY, linkName);

            return monoError(logBuilder, new AmqpException(true, "Cannot create receive link from a closed session.",
                sessionHandler.getErrorContext()));
        }

        final LinkSubscription<AmqpReceiveLink> existingLink = openReceiveLinks.get(linkName);
        if (existingLink != null) {
            logger.atInfo()
                .addKeyValue(LINK_NAME_KEY, linkName)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log("Returning existing receive link.");
            return Mono.just(existingLink.getLink());
        }

        final TokenManager tokenManager = tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath);
        return Mono.when(onActiveEndpoint(), tokenManager.authorize())
            .then(Mono.create((Consumer<MonoSink<AmqpReceiveLink>>) sink -> {
                try {
                    // This has to be executed using reactor dispatcher because it's possible to run into race
                    // conditions with proton-j.
                    provider.getReactorDispatcher().invoke(() -> {
                        final LinkSubscription<AmqpReceiveLink> computed
                            = openReceiveLinks.compute(linkName, (linkNameKey, existing) -> {
                                if (existing != null) {
                                    logger.atInfo()
                                        .addKeyValue(LINK_NAME_KEY, linkName)
                                        .log("Another receive link exists. Disposing of new one.");
                                    tokenManager.close();

                                    return existing;
                                }

                                logger.atInfo()
                                    .addKeyValue(SESSION_NAME_KEY, sessionName)
                                    .addKeyValue(LINK_NAME_KEY, linkName)
                                    .log("Creating a new receiver link.");

                                return getSubscription(linkNameKey, entityPath, sourceFilters, receiverProperties,
                                    receiverDesiredCapabilities, senderSettleMode, receiverSettleMode, tokenManager,
                                    consumerFactory);
                            });

                        sink.success(computed.getLink());
                    });
                } catch (IOException | RejectedExecutionException e) {
                    sink.error(e);
                }
            }))
            .onErrorResume(t -> Mono.error(() -> {
                tokenManager.close();
                return t;
            }));
    }

    /**
     * Creates an {@link AmqpLink} that has AMQP specific capabilities set.
     *
     * @param linkName Name of the receive link.
     * @param entityPath Address in the message broker for the link.
     * @param linkProperties The properties needed to be set on the link.
     * @param timeout Operation timeout when creating the link.
     * @param retry Retry policy to apply when link creation times out.
     *
     * @return A new instance of an {@link AmqpLink} with the correct properties set.
     */
    protected Mono<AmqpLink> createProducer(String linkName, String entityPath, Duration timeout, AmqpRetryPolicy retry,
        Map<Symbol, Object> linkProperties) {

        final Target target = new Target();
        target.setAddress(entityPath);

        final AmqpRetryOptions options
            = retry != null ? new AmqpRetryOptions(retry.getRetryOptions()) : new AmqpRetryOptions();

        if (timeout != null) {
            options.setTryTimeout(timeout);
        }

        return createProducer(linkName, entityPath, target, options, linkProperties, true).cast(AmqpLink.class);
    }

    private Mono<AmqpSendLink> createProducer(String linkName, String entityPath,
        org.apache.qpid.proton.amqp.transport.Target target, AmqpRetryOptions options,
        Map<Symbol, Object> linkProperties, boolean requiresAuthorization) {

        if (isDisposed()) {
            LoggingEventBuilder logBuilder = logger.atWarning()
                .addKeyValue(SESSION_NAME_KEY, sessionName)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue(LINK_NAME_KEY, linkName);

            return monoError(logBuilder, new AmqpException(true, "Cannot create send link from a closed session.",
                sessionHandler.getErrorContext()));
        }

        final LinkSubscription<AmqpSendLink> existing = openSendLinks.get(linkName);
        if (existing != null) {
            logger.atVerbose().addKeyValue(LINK_NAME_KEY, linkName).log("Returning existing send link.");
            return Mono.just(existing.getLink());
        }

        final TokenManager tokenManager;
        final Mono<Long> authorize;
        if (requiresAuthorization) {
            tokenManager = tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath);
            authorize = tokenManager.authorize();
        } else {
            tokenManager = null;
            authorize = Mono.empty();
        }

        return Mono.when(onActiveEndpoint(), authorize).then(Mono.create(sink -> {
            try {
                // We have to invoke this in the same thread or else proton-j will not properly link up the created
                // sender because the link names are not unique. Link name == entity path.
                provider.getReactorDispatcher().invoke(() -> {
                    final LinkSubscription<AmqpSendLink> computed
                        = openSendLinks.compute(linkName, (linkNameKey, existingLink) -> {
                            if (existingLink != null) {
                                logger.atInfo()
                                    .addKeyValue(LINK_NAME_KEY, linkName)
                                    .log("Another send link exists. Disposing of new one.");

                                if (tokenManager != null) {
                                    tokenManager.close();
                                }
                                return existingLink;
                            }

                            logger.atInfo()
                                .addKeyValue(LINK_NAME_KEY, linkName)
                                .addKeyValue(SESSION_NAME_KEY, sessionName)
                                .log("Creating a new send link.");

                            return getSubscription(linkName, entityPath, target, linkProperties, options, tokenManager);
                        });

                    sink.success(computed.getLink());
                });
            } catch (IOException | RejectedExecutionException e) {
                sink.error(e);
            }
        }));
    }

    /**
     * NOTE: Ensure this is invoked using the reactor dispatcher because proton-j is not thread-safe.
     */
    private LinkSubscription<AmqpSendLink> getSubscription(String linkName, String entityPath,
        org.apache.qpid.proton.amqp.transport.Target target, Map<Symbol, Object> linkProperties,
        AmqpRetryOptions options, TokenManager tokenManager) {

        final Sender sender = session.sender(linkName);
        sender.setTarget(target);
        sender.setSenderSettleMode(SenderSettleMode.UNSETTLED);

        final Source source = new Source();
        if (linkProperties != null && linkProperties.size() > 0) {
            final String clientIdentifier = (String) linkProperties.get(CLIENT_IDENTIFIER);
            if (!CoreUtils.isNullOrEmpty(clientIdentifier)) {
                source.setAddress(clientIdentifier);
                linkProperties.remove(CLIENT_IDENTIFIER);
            }
            sender.setProperties(linkProperties);
        }
        sender.setSource(source);

        final SendLinkHandler sendLinkHandler = handlerProvider.createSendLinkHandler(sessionHandler.getConnectionId(),
            sessionHandler.getHostname(), linkName, entityPath);
        BaseHandler.setHandler(sender, sendLinkHandler);

        sender.open();

        final AmqpSendLink reactorSender = linkProvider.createSendLink(amqpConnection, entityPath, sender,
            sendLinkHandler, provider, tokenManager, messageSerializer, options, timeoutScheduler,
            handlerProvider.getMetricProvider(amqpConnection.getFullyQualifiedNamespace(), entityPath));

        //@formatter:off
        final Disposable subscription = reactorSender.getEndpointStates().subscribe(state -> {
        }, error -> {
            if (!isDisposed.get()) {
                removeLink(openSendLinks, linkName);
            }
        }, () -> {
            if (!isDisposed.get()) {
                logger.atInfo()
                    .addKeyValue(LINK_NAME_KEY, linkName)
                    .log("Complete. Removing and disposing send link.");

                removeLink(openSendLinks, linkName);
            }
        });
        //@formatter:on

        return new LinkSubscription<>(reactorSender, subscription,
            String.format("connectionId[%s] session[%s]: Setting error on receive link.",
                sessionHandler.getConnectionId(), sessionName));
    }

    /**
     * NOTE: Ensure this is invoked using the reactor dispatcher because proton-j is not thread-safe.
     */
    private LinkSubscription<AmqpReceiveLink> getSubscription(String linkName, String entityPath,
        Map<Symbol, Object> sourceFilters, Map<Symbol, Object> receiverProperties, Symbol[] receiverDesiredCapabilities,
        SenderSettleMode senderSettleMode, ReceiverSettleMode receiverSettleMode, TokenManager tokenManager,
        ConsumerFactory consumerFactory) {

        final Receiver receiver = session.receiver(linkName);
        final Source source = new Source();
        source.setAddress(entityPath);

        if (sourceFilters != null && sourceFilters.size() > 0) {
            source.setFilter(sourceFilters);
        }

        receiver.setSource(source);

        // Use explicit settlement via dispositions (not pre-settled)
        receiver.setSenderSettleMode(senderSettleMode);
        receiver.setReceiverSettleMode(receiverSettleMode);

        final Target target = new Target();
        if (receiverProperties != null && !receiverProperties.isEmpty()) {
            receiver.setProperties(receiverProperties);
            final String clientIdentifier = (String) receiverProperties.get(CLIENT_RECEIVER_IDENTIFIER);
            if (!CoreUtils.isNullOrEmpty(clientIdentifier)) {
                target.setAddress(clientIdentifier);
            }
        }
        receiver.setTarget(target);

        if (receiverDesiredCapabilities != null && receiverDesiredCapabilities.length > 0) {
            receiver.setDesiredCapabilities(receiverDesiredCapabilities);
        }

        // When removing v1 receiver support, the type 'ConsumerFactory' will be deleted, and we'll replace
        // the logic here with the logic in ConsumerFactory.createConsumer' that uses the new v2 receiver types.
        final AmqpReceiveLink reactorReceiver = consumerFactory.createConsumer(amqpConnection, linkName, entityPath,
            receiver, tokenManager, provider, handlerProvider, linkProvider, retryOptions);

        final Disposable subscription = reactorReceiver.getEndpointStates().subscribe(state -> {
        }, error -> {
            if (!isDisposed.get()) {
                removeLink(openReceiveLinks, linkName);
            }
        }, () -> {
            if (!isDisposed.get()) {
                logger.atInfo()
                    .addKeyValue(LINK_NAME_KEY, linkName)
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .log("Complete. Removing receive link.");

                removeLink(openReceiveLinks, linkName);
            }
        });

        return new LinkSubscription<>(reactorReceiver, subscription, String.format(
            "connectionId[%s] sessionName[%s]: Setting error on receive link.", amqpConnection.getId(), sessionName));
    }

    /**
     * Returns a Mono that completes when the connection handler is closed. If it does, an {@link AmqpException} is
     * returned. It indicates that a shutdown was initiated and we should stop.
     *
     * @return A Mono that completes when the shutdown signal is emitted. If it does, returns an error.
     */
    private <T> Mono<T> onClosedError(String message, String linkName, String entityPath) {
        return Mono.firstWithSignal(isClosedMono.asMono(), shutdownSignals.next())
            .then(Mono.error(new AmqpException(false,
                String.format("connectionId[%s] entityPath[%s] linkName[%s] Connection closed. %s",
                    sessionHandler.getConnectionId(), entityPath, linkName, message),
                sessionHandler.getErrorContext())));
    }

    /**
     * Asynchronously waits for the session's active endpoint state.
     *
     * @return A mono that completes when the session is active.
     */
    private Mono<Void> onActiveEndpoint() {
        return RetryUtil
            .withRetry(getEndpointStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE), retryOptions,
                activeTimeoutMessage)
            .then();
    }

    private void handleClose() {
        logger.atVerbose()
            .addKeyValue(SESSION_NAME_KEY, sessionName)
            .log("Disposing of active send and receive links due to session close.");

        closeAsync().subscribe();
    }

    private void handleError(Throwable error) {
        logger.atVerbose().addKeyValue(SESSION_NAME_KEY, sessionName).log("Disposing of active links due to error.");

        final ErrorCondition condition;
        if (error instanceof AmqpException) {
            final AmqpException exception = ((AmqpException) error);
            final String errorCondition
                = exception.getErrorCondition() != null ? exception.getErrorCondition().getErrorCondition() : "UNKNOWN";
            condition = new ErrorCondition(Symbol.getSymbol(errorCondition), exception.getMessage());
        } else {
            condition = null;
        }

        closeAsync(error.getMessage(), condition, true).subscribe();
    }

    /**
     * Takes care of setting the error condition on the session, closing the children if specified and then waiting
     *
     * @param errorCondition Condition to set on the session.
     * @param disposeLinks {@code true} to dispose of children. {@code false} to ignore them, this may be the case
     *     when the {@link AmqpConnection} passes a shutdown signal.
     */
    private void disposeWork(ErrorCondition errorCondition, boolean disposeLinks) {
        if (session.getLocalState() != EndpointState.CLOSED) {
            session.close();

            if (errorCondition != null && session.getCondition() == null) {
                session.setCondition(errorCondition);
            }
        }

        final ArrayList<Mono<Void>> closingLinks = new ArrayList<>();
        if (disposeLinks) {
            synchronized (closeLock) {
                openReceiveLinks.values().forEach(link -> {
                    if (link == null) {
                        return;
                    }

                    closingLinks.add(link.closeAsync(errorCondition));
                });
                openSendLinks.values().forEach(link -> {
                    if (link == null) {
                        return;
                    }

                    closingLinks.add(link.closeAsync(errorCondition));
                });
            }
        }

        // We want to complete the session so that the parent connection isn't waiting.
        Mono<Void> closeLinksMono
            = Mono.when(closingLinks).timeout(retryOptions.getTryTimeout()).onErrorResume(error -> {
                logger.atWarning()
                    .addKeyValue(SESSION_NAME_KEY, sessionName)
                    .log("Timed out waiting for all links to close.", error);
                return Mono.empty();
            }).then(Mono.fromRunnable(() -> {
                isClosedMono.emitEmpty((signalType, result) -> {
                    addSignalTypeAndResult(logger.atWarning(), signalType, result)
                        .addKeyValue(SESSION_NAME_KEY, sessionName)
                        .log("Unable to emit shutdown signal.");

                    return false;
                });

                sessionHandler.close();
                subscriptions.dispose();
            }));

        subscriptions.add(closeLinksMono.subscribe());
    }

    private <T extends AmqpLink> boolean removeLink(ConcurrentMap<String, LinkSubscription<T>> openLinks, String key) {
        if (key == null) {
            return false;
        }

        synchronized (closeLock) {
            final LinkSubscription<T> removed = openLinks.remove(key);
            if (removed != null) {
                removed.closeAsync(null).subscribe();
            }

            return removed != null;
        }
    }

    private static final class LinkSubscription<T extends AmqpLink> {
        private final AtomicBoolean isDisposed = new AtomicBoolean();
        private final T link;
        private final Disposable subscription;
        private final String errorMessage;

        private LinkSubscription(T link, Disposable subscription, String errorMessage) {
            this.link = link;
            this.subscription = subscription;
            this.errorMessage = errorMessage;
        }

        public T getLink() {
            return link;
        }

        Mono<Void> closeAsync(ErrorCondition errorCondition) {
            if (isDisposed.getAndSet(true)) {
                return Mono.empty();
            }

            subscription.dispose();

            if (link instanceof ReactorReceiver) {
                return ((ReactorReceiver) link).closeAsync(errorMessage, errorCondition);
            } else if (link instanceof ReactorSender) {
                return ((ReactorSender) link).closeAsync(errorMessage, errorCondition);
            } else {
                link.dispose();
                return Mono.empty();
            }
        }
    }
}
