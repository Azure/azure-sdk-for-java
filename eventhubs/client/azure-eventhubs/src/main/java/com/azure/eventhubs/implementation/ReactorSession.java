// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpReceiveLink;
import com.azure.core.amqp.AmqpSendLink;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Session;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

class ReactorSession extends EndpointStateNotifierBase implements AmqpSession {
    private final Session session;
    private final SessionHandler handler;
    private final String sessionName;
    private final ReactorProvider provider;
    private final Duration openTimeout;
    private final Disposable.Composite subscriptions;

    ReactorSession(Session session, SessionHandler handler, String sessionName, ReactorProvider provider,
                   Duration openTimeout) {
        super(new ServiceLogger(ReactorSession.class));

        Objects.requireNonNull(session);
        Objects.requireNonNull(handler);
        Objects.requireNonNull(sessionName);
        Objects.requireNonNull(provider);
        Objects.requireNonNull(openTimeout);

        this.session = session;
        this.handler = handler;
        this.sessionName = sessionName;
        this.provider = provider;
        this.openTimeout = openTimeout;

        this.subscriptions = Disposables.composite(
            this.handler.getEndpointStates().subscribe(
                this::notifyEndpointState,
                error -> notifyError(handler.getContext(error)),
                () -> notifyEndpointState(EndpointState.CLOSED)),
            this.handler.getErrors().subscribe(
                this::notifyError,
                error -> notifyError(handler.getContext(error)),
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
    public Mono<AmqpSendLink> createSender(String linkName, String path, Duration timeout) {
        return null;
    }

    @Override
    public Mono<AmqpReceiveLink> createReceiver(String linkName, String path, Duration timeout) {
        return null;
    }

    @Override
    public boolean removeLink(String linkName) {
        return false;
    }
}
