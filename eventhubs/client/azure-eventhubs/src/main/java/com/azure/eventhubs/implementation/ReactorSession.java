// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.Retry;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.eventhubs.EventSender;
import com.azure.eventhubs.implementation.handler.ReceiveLinkHandler;
import com.azure.eventhubs.implementation.handler.SendLinkHandler;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.azure.eventhubs.implementation.AmqpConstants.VENDOR;

class ReactorSession extends EndpointStateNotifierBase implements EventHubSession {
    private static final Symbol ENABLE_RECEIVER_RUNTIME_METRIC_NAME = Symbol.valueOf(VENDOR + ":enable-receiver-runtime-metric");
    private static final Symbol EPOCH = Symbol.valueOf(VENDOR + ":epoch");
    private static final Symbol RECEIVER_IDENTIFIER_NAME = Symbol.valueOf(VENDOR + ":receiver-name");

    private final ConcurrentMap<String, AmqpSendLink> openSendLinks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AmqpReceiveLink> openReceiveLinks = new ConcurrentHashMap<>();

    private final Session session;
    private final SessionHandler sessionHandler;
    private final String sessionName;
    private final ReactorProvider provider;
    private final TokenResourceProvider audienceProvider;
    private final Duration openTimeout;
    private final Disposable.Composite subscriptions;
    private final ReactorHandlerProvider handlerProvider;
    private final Mono<CBSNode> cbsNodeSupplier;

    ReactorSession(Session session, SessionHandler sessionHandler, String sessionName, ReactorProvider provider,
                   ReactorHandlerProvider handlerProvider, Mono<CBSNode> cbsNodeSupplier,
                   TokenResourceProvider audienceProvider, Duration openTimeout) {
        super(new ServiceLogger(ReactorSession.class));
        this.session = session;
        this.sessionHandler = sessionHandler;
        this.handlerProvider = handlerProvider;
        this.sessionName = sessionName;
        this.provider = provider;
        this.cbsNodeSupplier = cbsNodeSupplier;
        this.audienceProvider = audienceProvider;
        this.openTimeout = openTimeout;

        this.subscriptions = Disposables.composite(
            this.sessionHandler.getEndpointStates().subscribe(
                this::notifyEndpointState,
                error -> notifyError(sessionHandler.getContext(error)),
                () -> notifyEndpointState(EndpointState.CLOSED)),
            this.sessionHandler.getErrors().subscribe(
                this::notifyError,
                error -> notifyError(sessionHandler.getContext(error)),
                () -> notifyEndpointState(EndpointState.CLOSED)));

        session.open();
    }

    Session session() {
        return this.session;
    }

    @Override
    public void close() {
        openReceiveLinks.forEach((key, link) -> {
            try {
                link.close();
            } catch (IOException e) {
                logger.asError().log("Error closing send link: " + key, e);
            }
        });
        openReceiveLinks.clear();

        openSendLinks.forEach((key, link) -> {
            try {
                link.close();
            } catch (IOException e) {
                logger.asError().log("Error closing receive link: " + key, e);
            }
        });
        openSendLinks.clear();
        subscriptions.dispose();
        super.close();
    }

    @Override
    public String getSessionName() {
        return sessionName;
    }

    @Override
    public Duration getOpenTimeout() {
        return openTimeout;
    }

    @Override
    public Mono<AmqpLink> createSender(String linkName, String entityPath, Duration timeout, Retry retry) {
        final ActiveClientTokenManager tokenManager = createTokenManager(entityPath);

        return getConnectionStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE)
            .timeout(timeout)
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

                final SendLinkHandler sendLinkHandler = handlerProvider.createSendLinkHandler(sessionHandler.getConnectionId(), sessionHandler.getHostname(), linkName);
                BaseHandler.setHandler(sender, sendLinkHandler);

                try {
                    provider.getReactorDispatcher().invoke(() -> {
                        sender.open();
                        final ReactorSender reactorSender = new ReactorSender(entityPath, sender, sendLinkHandler, provider, tokenManager, timeout, retry, EventSender.MAX_MESSAGE_LENGTH_BYTES);
                        openSendLinks.put(linkName, reactorSender);
                        sink.success(reactorSender);
                    });
                } catch (IOException e) {
                    sink.error(e);
                }
            })));
    }

    @Override
    public Mono<AmqpLink> createReceiver(String linkName, String entityPath, Duration timeout, Retry retry) {
        return createReceiver(linkName, entityPath, timeout, retry, null, false, null);
    }

    @Override
    public Mono<AmqpLink> createReceiver(String linkName, String entityPath, Duration timeout, Retry retry,
                                         Long receiverPriority, boolean keepPartitionInformationUpdated, String receiverIdentifier) {
        final ActiveClientTokenManager tokenManager = createTokenManager(entityPath);

        return getConnectionStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE)
            .timeout(timeout)
            .then(tokenManager.authorize().then(Mono.create(sink -> {
                final AmqpReceiveLink existingReceiver = openReceiveLinks.get(linkName);
                if (existingReceiver != null) {
                    sink.success(existingReceiver);
                    return;
                }

                final Receiver receiver = session.receiver(linkName);

                final Source source = new Source();
                source.setAddress(entityPath);

                //TODO (conniey): support this.
                // final Map<Symbol, UnknownDescribedType> filterMap = MessageReceiver.this.settingsProvider.getFilter(MessageReceiver.this.lastReceivedMessage);
                // if (filterMap != null) {
                //    source.setFilter(filterMap);
                // }

                receiver.setSource(source);

                final Target target = new Target();
                receiver.setTarget(target);

                // Use explicit settlement via dispositions (not pre-settled)
                receiver.setSenderSettleMode(SenderSettleMode.UNSETTLED);
                receiver.setReceiverSettleMode(ReceiverSettleMode.SECOND);

                Map<Symbol, Object> properties = new HashMap<>();
                if (receiverPriority != null) {
                    properties.put(EPOCH, receiverPriority);
                }
                if (!ImplUtils.isNullOrEmpty(receiverIdentifier)) {
                    properties.put(RECEIVER_IDENTIFIER_NAME, receiverIdentifier);
                }
                if (!properties.isEmpty()) {
                    receiver.setProperties(properties);
                }

                if (keepPartitionInformationUpdated) {
                    receiver.setDesiredCapabilities(new Symbol[]{ENABLE_RECEIVER_RUNTIME_METRIC_NAME});
                }

                final ReceiveLinkHandler receiveLinkHandler = handlerProvider.createReceiveLinkHandler(sessionHandler.getConnectionId(), sessionHandler.getHostname(), linkName);
                BaseHandler.setHandler(receiver, receiveLinkHandler);

                try {
                    provider.getReactorDispatcher().invoke(() -> {
                        receiver.open();

                        final ReactorReceiver reactorReceiver = new ReactorReceiver(entityPath, receiver, receiveLinkHandler, tokenManager);

                        openReceiveLinks.put(linkName, reactorReceiver);
                        sink.success(reactorReceiver);
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
    public boolean removeLink(String linkName) {
        return (openSendLinks.remove(linkName) != null) || openReceiveLinks.remove(linkName) != null;
    }

    private ActiveClientTokenManager createTokenManager(String entityPath) {
        final String tokenAudience = audienceProvider.getResourceString(entityPath);
        return new ActiveClientTokenManager(cbsNodeSupplier, tokenAudience, ClientConstants.TOKEN_REFRESH_INTERVAL);
    }
}
