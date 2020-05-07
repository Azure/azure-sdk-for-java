// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents an AMQP session using proton-j reactor.
 */
public class ReactorSession implements AmqpSession {
    private final ConcurrentMap<String, LinkSubscription<AmqpSendLink>> openSendLinks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LinkSubscription<AmqpReceiveLink>> openReceiveLinks = new ConcurrentHashMap<>();

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(ReactorSession.class);
    private final ReplayProcessor<AmqpEndpointState> endpointStates =
        ReplayProcessor.cacheLastOrDefault(AmqpEndpointState.UNINITIALIZED);
    private FluxSink<AmqpEndpointState> endpointStateSink = endpointStates.sink(FluxSink.OverflowStrategy.BUFFER);

    private final Session session;
    private final SessionHandler sessionHandler;
    private final String sessionName;
    private final ReactorProvider provider;
    private final TokenManagerProvider tokenManagerProvider;
    private final MessageSerializer messageSerializer;
    private final Duration openTimeout;

    private final Disposable.Composite subscriptions;
    private final ReactorHandlerProvider handlerProvider;
    private final Mono<ClaimsBasedSecurityNode> cbsNodeSupplier;

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
     * @param openTimeout Timeout to wait for the session operation to complete.
     */
    public ReactorSession(Session session, SessionHandler sessionHandler, String sessionName, ReactorProvider provider,
        ReactorHandlerProvider handlerProvider, Mono<ClaimsBasedSecurityNode> cbsNodeSupplier,
        TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer,
        Duration openTimeout) {
        this.session = session;
        this.sessionHandler = sessionHandler;
        this.handlerProvider = handlerProvider;
        this.sessionName = sessionName;
        this.provider = provider;
        this.cbsNodeSupplier = cbsNodeSupplier;
        this.tokenManagerProvider = tokenManagerProvider;
        this.messageSerializer = messageSerializer;
        this.openTimeout = openTimeout;

        this.subscriptions = Disposables.composite(
            this.sessionHandler.getEndpointStates().subscribe(
                state -> {
                    logger.verbose("Connection state: {}", state);
                    endpointStateSink.next(AmqpEndpointStateUtil.getConnectionState(state));
                }, error -> {
                    logger.error("[{}] Error occurred in session endpoint handler.", sessionName, error);
                    endpointStateSink.error(error);
                    dispose();
                }, () -> {
                    endpointStateSink.next(AmqpEndpointState.CLOSED);
                    endpointStateSink.complete();
                    dispose();
                }),

            this.sessionHandler.getErrors().subscribe(error -> {
                logger.error("[{}] Error occurred in session error handler.", sessionName, error);
                endpointStateSink.error(error);
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
        if (isDisposed.getAndSet(true)) {
            return;
        }

        logger.info("sessionId[{}]: Disposing of session.", sessionName);

        session.close();
        subscriptions.dispose();

        openReceiveLinks.forEach((key, link) -> link.dispose());
        openReceiveLinks.clear();

        openSendLinks.forEach((key, link) -> link.dispose());
        openSendLinks.clear();
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
        return openTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpLink> createProducer(String linkName, String entityPath, Duration timeout, AmqpRetryPolicy retry) {
        if (isDisposed()) {
            return Mono.error(logger.logExceptionAsError(new IllegalStateException(String.format(
                "Cannot create send link '%s' from a closed session. entityPath[%s]", linkName, entityPath))));
        }

        final LinkSubscription<AmqpSendLink> existing = openSendLinks.get(linkName);
        if (existing != null) {
            logger.verbose("linkName[{}]: Returning existing send link.", linkName);
            return Mono.just(existing.getLink());
        }

        final TokenManager tokenManager = tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath);
        return RetryUtil.withRetry(
            getEndpointStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE),
            timeout, retry)
            .then(tokenManager.authorize().then(Mono.<AmqpLink>create(sink -> {
                try {
                    // We have to invoke this in the same thread or else proton-j will not properly link up the created
                    // sender because the link names are not unique. Link name == entity path.
                    provider.getReactorDispatcher().invoke(() -> {
                        final LinkSubscription<AmqpSendLink> computed = openSendLinks.compute(linkName,
                            (linkNameKey, existingLink) -> {
                                if (existingLink != null) {
                                    logger.info("linkName[{}]: Another send link exists. Disposing of new one.",
                                        linkName);
                                    tokenManager.close();
                                    return existingLink;
                                }

                                return getSubscription(linkNameKey, entityPath, timeout, retry, tokenManager);
                            });

                        sink.success(computed.getLink());
                    });
                } catch (IOException e) {
                    sink.error(e);
                }
            })));
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

    private <T extends AmqpLink> boolean removeLink(ConcurrentMap<String, LinkSubscription<T>> openLinks, String key) {
        if (key == null) {
            return false;
        }

        final LinkSubscription<T> removed = openLinks.remove(key);
        if (removed != null) {
            removed.dispose();
        }

        return removed != null;
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
        AmqpRetryPolicy retry, Map<Symbol, Object> sourceFilters,
        Map<Symbol, Object> receiverProperties, Symbol[] receiverDesiredCapabilities, SenderSettleMode senderSettleMode,
        ReceiverSettleMode receiverSettleMode) {

        if (isDisposed()) {
            return Mono.error(logger.logExceptionAsError(new IllegalStateException(String.format(
                "Cannot create send link '%s' from a closed session. entityPath[%s]", linkName, entityPath))));
        }

        final LinkSubscription<AmqpReceiveLink> existingLink = openReceiveLinks.get(linkName);
        if (existingLink != null) {
            logger.info("linkName[{}] entityPath[{}]: Returning existing receive link.", linkName, entityPath);
            return Mono.just(existingLink.getLink());
        }

        final TokenManager tokenManager = tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath);
        return RetryUtil.withRetry(
            getEndpointStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE), timeout, retry)
            .then(tokenManager.authorize().then(Mono.create(sink -> {
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

                                return getSubscription(linkNameKey, entityPath, sourceFilters, receiverProperties,
                                    receiverDesiredCapabilities, senderSettleMode, receiverSettleMode, tokenManager);
                            });

                        sink.success(computed.getLink());
                    });
                } catch (IOException e) {
                    sink.error(e);
                }
            })));
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
     * NOTE: Ensure this is invoked using the reactor dispatcher because proton-j is not thread-safe.
     */
    private LinkSubscription<AmqpSendLink> getSubscription(String linkName, String entityPath, Duration timeout,
        AmqpRetryPolicy retry, TokenManager tokenManager) {
        final Sender sender = session.sender(linkName);
        final Target target = new Target();

        target.setAddress(entityPath);
        sender.setTarget(target);

        final Source source = new Source();
        sender.setSource(source);
        sender.setSenderSettleMode(SenderSettleMode.UNSETTLED);

        final SendLinkHandler sendLinkHandler = handlerProvider.createSendLinkHandler(
            sessionHandler.getConnectionId(), sessionHandler.getHostname(), linkName, entityPath);
        BaseHandler.setHandler(sender, sendLinkHandler);

        sender.open();

        final ReactorSender reactorSender = new ReactorSender(entityPath, sender, sendLinkHandler, provider,
            tokenManager, messageSerializer, timeout, retry);

        final Disposable subscription = reactorSender.getEndpointStates().subscribe(state -> {
        }, error -> {
                logger.info("linkName[{}]: Error occurred. Removing and disposing send link.",
                    linkName, error);
                removeLink(openSendLinks, linkName);
            }, () -> {
                logger.info("linkName[{}]: Complete. Removing and disposing send link.", linkName);
                removeLink(openSendLinks, linkName);
            });

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

    private static final class LinkSubscription<T extends AmqpLink> implements Disposable {
        private final AtomicBoolean isDisposed = new AtomicBoolean();
        private final T link;
        private final Disposable subscription;

        private LinkSubscription(T link, Disposable subscription) {
            this.link = link;
            this.subscription = subscription;
        }

        public Disposable getSubscription() {
            return subscription;
        }

        public T getLink() {
            return link;
        }

        @Override
        public void dispose() {
            if (isDisposed.getAndSet(true)) {
                return;
            }

            subscription.dispose();
            link.dispose();
        }
    }
}
