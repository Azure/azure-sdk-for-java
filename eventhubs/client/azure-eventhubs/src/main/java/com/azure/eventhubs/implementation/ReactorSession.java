// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.Retry;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.EventSender;
import com.azure.eventhubs.implementation.handler.SendLinkHandler;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class ReactorSession extends EndpointStateNotifierBase implements AmqpSession {
    private final ConcurrentMap<String, AmqpSendLink> openSendLinks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AmqpReceiveLink> openReceiveLinks = new ConcurrentHashMap<>();

    private final Session session;
    private final SessionHandler sessionHandler;
    private final String sessionName;
    private final ReactorProvider provider;
    private final Duration openTimeout;
    private final Disposable.Composite subscriptions;
    private final ReactorHandlerProvider handlerProvider;
    private final Mono<CBSNode> cbsNodeSupplier;

    ReactorSession(Session session, SessionHandler sessionHandler, String sessionName, ReactorProvider provider,
                   ReactorHandlerProvider handlerProvider, Mono<CBSNode> cbsNodeSupplier, Duration openTimeout) {
        super(new ServiceLogger(ReactorSession.class));
        this.session = session;
        this.sessionHandler = sessionHandler;
        this.handlerProvider = handlerProvider;
        this.sessionName = sessionName;
        this.provider = provider;
        this.cbsNodeSupplier = cbsNodeSupplier;
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
        final String tokenAudience = String.format(Locale.US, ClientConstants.TOKEN_AUDIENCE_FORMAT, sessionHandler.getHostname(), entityPath);
        final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(cbsNodeSupplier, tokenAudience, ClientConstants.TOKEN_VALIDITY, ClientConstants.TOKEN_REFRESH_INTERVAL);

        return getConnectionStates().takeUntil(state -> state == AmqpEndpointState.ACTIVE)
            .timeout(timeout)
            .then(tokenManager.authorize().then(Mono.create(sink -> {
                AmqpSendLink existingSender = openSendLinks.get(linkName);
                if (existingSender != null) {
                    sink.success(existingSender);
                    return;
                }

                try {
                    final Sender sender = session.sender(linkName);
                    final Target target = new Target();

                    target.setAddress(entityPath);
                    sender.setTarget(target);

                    final Source source = new Source();
                    sender.setSource(source);
                    sender.setSenderSettleMode(SenderSettleMode.UNSETTLED);

                    final SendLinkHandler sendLinkHandler = handlerProvider.createSendLinkHandler(sessionHandler.getConnectionId(), sessionHandler.getHostname(), linkName);
                    BaseHandler.setHandler(sender, sendLinkHandler);

                    provider.getReactorDispatcher().invoke(() -> {
                        sender.open();
                        final ReactorSender reactorSender = new ReactorSender(entityPath, sender, sendLinkHandler, provider, tokenManager, timeout, retry, EventSender.MAX_MESSAGE_LENGTH_BYTES);

                        sink.success(reactorSender);
                    });
                } catch (IOException e) {
                    sink.error(e);
                }
            })));
    }

    @Override
    public Mono<AmqpLink> createReceiver(String linkName, String entityPath, Duration timeout, Retry retry) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeLink(String linkName) {
        return (openSendLinks.remove(linkName) != null) || openReceiveLinks.remove(linkName) != null;
    }
}
