// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.Retry;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.EventSender;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Session;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;

import java.time.Duration;
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
    private final Mono<CBSNode> cbsNodeMono;

    ReactorSession(Session session, SessionHandler sessionHandler, String sessionName, ReactorProvider provider,
                   ReactorHandlerProvider handlerProvider, Mono<CBSNode> cbsNodeMono, Duration openTimeout) {
        super(new ServiceLogger(ReactorSession.class));
        this.session = session;
        this.sessionHandler = sessionHandler;
        this.handlerProvider = handlerProvider;
        this.sessionName = sessionName;
        this.provider = provider;
        this.cbsNodeMono = cbsNodeMono;
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
        return sessionHandler.getEndpointStates().takeUntil(state -> state == EndpointState.ACTIVE)
            .then(Mono.fromCallable(() -> openSendLinks.computeIfAbsent(linkName, key -> {
                return new ReactorSender(entityPath,
                    session.sender(linkName),
                    handlerProvider.createSendLinkHandler(sessionHandler.getConnectionId(), sessionHandler.getHostname(), linkName),
                    provider,
                    new ActiveClientTokenManager(cbsNodeMono, entityPath, ClientConstants.TOKEN_VALIDITY, ClientConstants.TOKEN_REFRESH_INTERVAL),
                    timeout, retry, EventSender.MAX_MESSAGE_LENGTH_BYTES);
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
