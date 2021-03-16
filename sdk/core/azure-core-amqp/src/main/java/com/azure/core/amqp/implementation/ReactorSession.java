// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
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

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;

/**
 * Represents an AMQP session using proton-j reactor.
 */
public class ReactorSession implements AmqpSession {
    private static final String TRANSACTION_LINK_NAME = "coordinator";
    private final ConcurrentMap<String, LinkSubscription<AmqpSendLink>> openSendLinks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LinkSubscription<AmqpReceiveLink>> openReceiveLinks = new ConcurrentHashMap<>();

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(ReactorSession.class);
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
    private final Mono<ClaimsBasedSecurityNode> cbsNodeSupplier;
    private final Disposable.Composite connectionSubscriptions;

    private final AtomicReference<TransactionCoordinator> transactionCoordinator = new AtomicReference<>();

    /**
     * Creates a new AMQP session using proton-j.
     *
     * @param session Proton-j session for this AMQP session.
     * @param sessionHandler Handler for events that occur in the session.
     * @param sessionName Name of the session.
     * @param provider Provides reactor instances for messages to sent with.
     * @param handlerProvider Providers reactor handlers for listening to proton-j reactor events.
     * @param cbsNodeSupplier Mono that returns a reference to the {@link ClaimsBasedSecurityNode}.
     * @param tokenManagerProvider Provides {@link TokenManager} that authorizes the client when performing
     *     operations on the message broker.
     * @param retryOptions for the session operations.
     */
    public ReactorSession(AmqpConnection amqpConnection, Session session, SessionHandler sessionHandler,
        String sessionName, ReactorProvider provider, ReactorHandlerProvider handlerProvider,
        Mono<ClaimsBasedSecurityNode> cbsNodeSupplier, TokenManagerProvider tokenManagerProvider,
        MessageSerializer messageSerializer, AmqpRetryOptions retryOptions) {
        this.amqpConnection = amqpConnection;

        this.session = session;
        this.sessionHandler = sessionHandler;
        this.handlerProvider = handlerProvider;
        this.sessionName = sessionName;
        this.provider = provider;
        this.cbsNodeSupplier = cbsNodeSupplier;
        this.tokenManagerProvider = tokenManagerProvider;
        this.messageSerializer = messageSerializer;
        this.retryOptions = retryOptions;
        this.activeTimeoutMessage = String.format(
            "ReactorSession connectionId[%s], session[%s]: Retries exhausted waiting for ACTIVE endpoint state.",
            sessionHandler.getConnectionId(), sessionName);

        this.endpointStates = sessionHandler.getEndpointStates()
            .map(state -> {
                logger.verbose("connectionId[{}], sessionName[{}], state[{}]", sessionHandler.getConnectionId(),
                    sessionName, state);
                return AmqpEndpointStateUtil.getConnectionState(state);
            })
            .cache(1);

        this.connectionSubscriptions = Disposables.composite(
            amqpConnection.getEndpointStates().subscribe(state -> {
                },
                error -> {
                    if (error instanceof AmqpException) {
                        final AmqpException amqpException = (AmqpException) error;
                        final ErrorCondition condition = new ErrorCondition(
                            Symbol.getSymbol(amqpException.getErrorCondition().getErrorCondition()),
                            amqpException.getMessage());
                        dispose(condition);
                    } else {
                        logger.warning("Exception was not an AmqpException.", error);
                        dispose(new ErrorCondition(Symbol.getSymbol("connection-error"), error.getMessage()));
                    }
                }, () -> {
                    logger.verbose("Connection states closed. Disposing normally.");
                    dispose();
                }),
            amqpConnection.getShutdownSignals().subscribe(signal -> {
                logger.verbose("Connection shutdown signal. Disposing of children.");
                dispose();
            }));

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
        dispose(null);
    }

    void dispose(ErrorCondition errorCondition) {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        try {
            provider.getReactorDispatcher().invoke(() -> disposeWork(errorCondition));
        } catch (IOException e) {
            logger.warning("Error occurred while scheduling work. Manually disposing.", e);
            disposeWork(errorCondition);
        }
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
        return createTransactionCoordinator()
            .flatMap(coordinator -> coordinator.createTransaction());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> commitTransaction(AmqpTransaction transaction) {
        return createTransactionCoordinator()
            .flatMap(coordinator -> coordinator.completeTransaction(transaction, true));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> rollbackTransaction(AmqpTransaction transaction) {
        return createTransactionCoordinator()
            .flatMap(coordinator -> coordinator.completeTransaction(transaction, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpLink> createProducer(String linkName, String entityPath, Duration timeout, AmqpRetryPolicy retry) {
        return createProducer(linkName, entityPath, timeout, retry, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpLink> createConsumer(String linkName, String entityPath, Duration timeout, AmqpRetryPolicy retry) {
        return createConsumer(linkName, entityPath, timeout, retry, null, null, null,
            SenderSettleMode.UNSETTLED, ReceiverSettleMode.SECOND)
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
     * @return {@link Mono} of {@link TransactionCoordinator}
     */
    private Mono<TransactionCoordinator> createTransactionCoordinator() {
        if (isDisposed()) {
            return Mono.error(logger.logExceptionAsError(new IllegalStateException(String.format(
                "Cannot create coordinator send link '%s' from a closed session.", TRANSACTION_LINK_NAME))));
        }

        final TransactionCoordinator existing = transactionCoordinator.get();
        if (existing != null) {
            logger.verbose("Coordinator[{}]: Returning existing transaction coordinator.", TRANSACTION_LINK_NAME);
            return Mono.just(existing);
        }

        return createProducer(TRANSACTION_LINK_NAME, TRANSACTION_LINK_NAME, new Coordinator(), retryOptions, null,
            false)
            .map(link -> {
                final TransactionCoordinator newCoordinator = new TransactionCoordinator(link, messageSerializer);
                if (transactionCoordinator.compareAndSet(null, newCoordinator)) {
                    return newCoordinator;
                } else {
                    return transactionCoordinator.get();
                }
            });
    }

    /**
     * Creates an {@link AmqpReceiveLink} that has AMQP specific capabilities set.
     *
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
     *
     * @return A new instance of an {@link AmqpReceiveLink} with the correct properties set.
     */
    protected Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, Duration timeout,
        AmqpRetryPolicy retry, Map<Symbol, Object> sourceFilters, Map<Symbol, Object> receiverProperties,
        Symbol[] receiverDesiredCapabilities, SenderSettleMode senderSettleMode,
        ReceiverSettleMode receiverSettleMode) {

        if (isDisposed()) {
            return Mono.error(logger.logExceptionAsError(new IllegalStateException(String.format(
                "Cannot create receive link '%s' from a closed session. entityPath[%s]", linkName, entityPath))));
        }

        final LinkSubscription<AmqpReceiveLink> existingLink = openReceiveLinks.get(linkName);
        if (existingLink != null) {
            logger.info("linkName[{}] entityPath[{}]: Returning existing receive link.", linkName, entityPath);
            return Mono.just(existingLink.getLink());
        }

        final TokenManager tokenManager = tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath);
        return Mono.when(onActiveEndpoint(), tokenManager.authorize()).then(Mono.create(sink -> {
            try {
                // This has to be executed using reactor dispatcher because it's possible to run into race
                // conditions with proton-j.
                provider.getReactorDispatcher().invoke(() -> {
                    final LinkSubscription<AmqpReceiveLink> computed = openReceiveLinks.compute(linkName,
                        (linkNameKey, existing) -> {
                            if (existing != null) {
                                logger.info("linkName[{}]: Another receive link exists. Disposing of new one.",
                                    linkName);
                                tokenManager.close();

                                return existing;
                            }

                            logger.info("Creating a new receiver link with linkName {}", linkName);
                            return getSubscription(linkNameKey, entityPath, sourceFilters, receiverProperties,
                                receiverDesiredCapabilities, senderSettleMode, receiverSettleMode, tokenManager);
                        });

                    sink.success(computed.getLink());
                });
            } catch (IOException e) {
                sink.error(e);
            }
        }));
    }

    /**
     * Given the entity path, associated receiver and link handler, creates the receive link instance.
     */
    protected ReactorReceiver createConsumer(String entityPath, Receiver receiver,
        ReceiveLinkHandler receiveLinkHandler, TokenManager tokenManager, ReactorProvider reactorProvider) {
        return new ReactorReceiver(entityPath, receiver, receiveLinkHandler, tokenManager,
            reactorProvider.getReactorDispatcher());
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
    protected Mono<AmqpLink> createProducer(String linkName, String entityPath, Duration timeout,
        AmqpRetryPolicy retry, Map<Symbol, Object> linkProperties) {

        final Target target = new Target();
        target.setAddress(entityPath);

        final AmqpRetryOptions options = retry != null
            ? new AmqpRetryOptions(retry.getRetryOptions())
            : new AmqpRetryOptions();

        if (timeout != null) {
            options.setTryTimeout(timeout);
        }

        return createProducer(linkName, entityPath, target, options, linkProperties, true)
            .cast(AmqpLink.class);
    }

    private Mono<AmqpSendLink> createProducer(String linkName, String entityPath,
        org.apache.qpid.proton.amqp.transport.Target target, AmqpRetryOptions options,
        Map<Symbol, Object> linkProperties, boolean requiresAuthorization) {

        if (isDisposed()) {
            return Mono.error(logger.logExceptionAsError(new IllegalStateException(String.format(
                "Cannot create send link '%s' from a closed session. entityPath[%s]", linkName, entityPath))));
        }

        final LinkSubscription<AmqpSendLink> existing = openSendLinks.get(linkName);
        if (existing != null) {
            logger.verbose("linkName[{}]: Returning existing send link.", linkName);
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
                    final LinkSubscription<AmqpSendLink> computed = openSendLinks.compute(linkName,
                        (linkNameKey, existingLink) -> {
                            if (existingLink != null) {
                                logger.info("linkName[{}]: Another send link exists. Disposing of new one.",
                                    linkName);

                                if (tokenManager != null) {
                                    tokenManager.close();
                                }
                                return existingLink;
                            }

                            logger.info("Creating a new sender link with linkName {}", linkName);
                            return getSubscription(linkName, entityPath, target, linkProperties, options,
                                tokenManager);
                        });

                    sink.success(computed.getLink());
                });
            } catch (IOException e) {
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

        final Source source = new Source();
        sender.setSource(source);
        sender.setSenderSettleMode(SenderSettleMode.UNSETTLED);

        if (linkProperties != null && linkProperties.size() > 0) {
            sender.setProperties(linkProperties);
        }

        final SendLinkHandler sendLinkHandler = handlerProvider.createSendLinkHandler(
            sessionHandler.getConnectionId(), sessionHandler.getHostname(), linkName, entityPath);
        BaseHandler.setHandler(sender, sendLinkHandler);

        sender.open();

        final ReactorSender reactorSender = new ReactorSender(amqpConnection, entityPath, sender, sendLinkHandler,
            provider, tokenManager, messageSerializer, options);

        //@formatter:off
        final Disposable subscription = reactorSender.getEndpointStates().subscribe(state -> {
        }, error -> {
            logger.info("linkName[{}]: Error occurred. Removing and disposing send link.",
                linkName, error);
            removeLink(openSendLinks, linkName);
        }, () -> {
            logger.info("linkName[{}]: Complete. Removing and disposing send link.", linkName);
            removeLink(openSendLinks, linkName);
        });
        //@formatter:on

        return new LinkSubscription<>(reactorSender, subscription);
    }

    /**
     * NOTE: Ensure this is invoked using the reactor dispatcher because proton-j is not thread-safe.
     */
    private LinkSubscription<AmqpReceiveLink> getSubscription(String linkName, String entityPath,
        Map<Symbol, Object> sourceFilters, Map<Symbol, Object> receiverProperties,
        Symbol[] receiverDesiredCapabilities, SenderSettleMode senderSettleMode, ReceiverSettleMode receiverSettleMode,
        TokenManager tokenManager) {

        final Receiver receiver = session.receiver(linkName);
        final Source source = new Source();
        source.setAddress(entityPath);

        if (sourceFilters != null && sourceFilters.size() > 0) {
            source.setFilter(sourceFilters);
        }

        receiver.setSource(source);

        final Target target = new Target();
        receiver.setTarget(target);

        // Use explicit settlement via dispositions (not pre-settled)
        receiver.setSenderSettleMode(senderSettleMode);
        receiver.setReceiverSettleMode(receiverSettleMode);

        if (receiverProperties != null && !receiverProperties.isEmpty()) {
            receiver.setProperties(receiverProperties);
        }

        if (receiverDesiredCapabilities != null && receiverDesiredCapabilities.length > 0) {
            receiver.setDesiredCapabilities(receiverDesiredCapabilities);
        }

        final ReceiveLinkHandler receiveLinkHandler = handlerProvider.createReceiveLinkHandler(
            sessionHandler.getConnectionId(), sessionHandler.getHostname(), linkName, entityPath);
        BaseHandler.setHandler(receiver, receiveLinkHandler);

        receiver.open();

        final ReactorReceiver reactorReceiver = createConsumer(entityPath, receiver, receiveLinkHandler,
            tokenManager, provider);

        final Disposable subscription = reactorReceiver.getEndpointStates().subscribe(state -> {
        }, error -> {
            logger.info(
                "linkName[{}] entityPath[{}]: Error occurred. Removing receive link.",
                linkName, entityPath, error);

            removeLink(openReceiveLinks, linkName);
        }, () -> {
            logger.info("linkName[{}] entityPath[{}]: Complete. Removing receive link.",
                linkName, entityPath);

            removeLink(openReceiveLinks, linkName);
        });

        return new LinkSubscription<>(reactorReceiver, subscription);
    }

    /**
     * Asynchronously waits for the session's active endpoint state.
     *
     * @return A mono that completes when the session is active.
     */
    private Mono<Void> onActiveEndpoint() {
        return RetryUtil.withRetry(getEndpointStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE),
            retryOptions, activeTimeoutMessage)
            .then();
    }

    private void disposeWork(ErrorCondition errorCondition) {
        logger.info("connectionId[{}], sessionId[{}], errorCondition[{}]: Disposing of session.",
            sessionHandler.getConnectionId(), sessionName,
            errorCondition != null ? errorCondition : NOT_APPLICABLE);

        connectionSubscriptions.dispose();

        if (session.getLocalState() != EndpointState.CLOSED) {
            session.close();

            if (errorCondition != null && session.getCondition() == null) {
                session.setCondition(errorCondition);
            }
        }

        openReceiveLinks.forEach((key, link) -> link.dispose(errorCondition));
        openSendLinks.forEach((key, link) -> link.dispose(errorCondition));
    }

    private <T extends AmqpLink> boolean removeLink(ConcurrentMap<String, LinkSubscription<T>> openLinks, String key) {
        if (key == null) {
            return false;
        }

        final LinkSubscription<T> removed = openLinks.remove(key);
        if (removed != null) {
            removed.dispose(null);
        }

        return removed != null;
    }

    private static final class LinkSubscription<T extends AmqpLink> {
        private final AtomicBoolean isDisposed = new AtomicBoolean();
        private final T link;
        private final Disposable subscription;

        private LinkSubscription(T link, Disposable subscription) {
            this.link = link;
            this.subscription = subscription;
        }

        public T getLink() {
            return link;
        }

        void dispose(ErrorCondition errorCondition) {
            if (isDisposed.getAndSet(true)) {
                return;
            }

            if (link instanceof ReactorReceiver) {
                final ReactorReceiver reactorReceiver = (ReactorReceiver) link;
                reactorReceiver.dispose(errorCondition);
            } else if (link instanceof ReactorSender) {
                final ReactorSender reactorSender = (ReactorSender) link;
                reactorSender.disposeAsync("Error in session. Disposing receiver.", errorCondition);
            } else {
                link.dispose();
            }

            subscription.dispose();
        }
    }
}
