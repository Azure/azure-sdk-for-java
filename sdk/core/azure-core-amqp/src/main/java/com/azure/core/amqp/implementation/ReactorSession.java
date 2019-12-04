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
import org.apache.qpid.proton.amqp.UnknownDescribedType;
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
    private final ConcurrentMap<String, AmqpSendLink> openSendLinks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AmqpReceiveLink> openReceiveLinks = new ConcurrentHashMap<>();
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
     * @param tokenManagerProvider Provides {@link TokenManager} that authorizes the client when performing operations
     *      on the message broker.
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
                    logger.error("Error occurred in connection.", error);
                    endpointStateSink.error(error);
                }, () -> {
                    endpointStateSink.next(AmqpEndpointState.CLOSED);
                    endpointStateSink.complete();
                }),

            this.sessionHandler.getErrors().subscribe(error -> {
                logger.error("Error occurred in connection.", error);
                endpointStateSink.error(error);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        openReceiveLinks.forEach((key, link) -> link.close());
        openReceiveLinks.clear();

        openSendLinks.forEach((key, link) -> link.close());
        openSendLinks.clear();
        subscriptions.dispose();
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
        final TokenManager tokenManager = tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath);

        return RetryUtil.withRetry(
            getEndpointStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE),
            timeout, retry)
            .then(tokenManager.authorize().then(Mono.create(sink -> {
                final AmqpSendLink existingSender = openSendLinks.get(linkName);
                if (existingSender != null) {
                    sink.success(existingSender);
                    return;
                }

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

                try {
                    provider.getReactorDispatcher().invoke(() -> {
                        sender.open();
                        final ReactorSender reactorSender =
                            new ReactorSender(entityPath, sender, sendLinkHandler, provider, tokenManager,
                                messageSerializer, timeout, retry, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
                        openSendLinks.put(linkName, reactorSender);
                        sink.success(reactorSender);
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
        return createConsumer(linkName, entityPath, timeout, retry, null, null, null)
            .cast(AmqpLink.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeLink(String linkName) {
        return (openSendLinks.remove(linkName) != null) || openReceiveLinks.remove(linkName) != null;
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
     * @param receiverProperties Any properties to associate with the receive link when attaching to message broker.
     * @param receiverDesiredCapabilities Capabilities that the receiver link supports.
     * @return A new instance of an {@link AmqpReceiveLink} with the correct properties set.
     */
    protected Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, Duration timeout,
            AmqpRetryPolicy retry, Map<Symbol, UnknownDescribedType> sourceFilters,
            Map<Symbol, Object> receiverProperties, Symbol[] receiverDesiredCapabilities) {
        final TokenManager tokenManager = tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath);

        return RetryUtil.withRetry(
            getEndpointStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE), timeout, retry)
            .then(tokenManager.authorize().then(Mono.create(sink -> {
                final AmqpReceiveLink existingReceiver = openReceiveLinks.get(linkName);
                if (existingReceiver != null) {
                    sink.success(existingReceiver);
                    return;
                }

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
                receiver.setSenderSettleMode(SenderSettleMode.UNSETTLED);
                receiver.setReceiverSettleMode(ReceiverSettleMode.SECOND);

                if (receiverProperties != null && !receiverProperties.isEmpty()) {
                    receiver.setProperties(receiverProperties);
                }

                if (receiverDesiredCapabilities != null && receiverDesiredCapabilities.length > 0) {
                    receiver.setDesiredCapabilities(receiverDesiredCapabilities);
                }

                final ReceiveLinkHandler receiveLinkHandler = handlerProvider.createReceiveLinkHandler(
                    sessionHandler.getConnectionId(), sessionHandler.getHostname(), linkName, entityPath);
                BaseHandler.setHandler(receiver, receiveLinkHandler);

                try {
                    provider.getReactorDispatcher().invoke(() -> {
                        receiver.open();

                        final ReactorReceiver reactorReceiver =
                            new ReactorReceiver(entityPath, receiver, receiveLinkHandler, tokenManager);

                        openReceiveLinks.put(linkName, reactorReceiver);
                        sink.success(reactorReceiver);
                    });
                } catch (IOException e) {
                    sink.error(e);
                }
            })));
    }
}
