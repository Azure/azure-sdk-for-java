// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Session;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;

import java.time.Duration;

class ReactorSession extends StateNotifierBase implements AmqpSession {
    private final Session session;
    private final SessionHandler handler;
    private final String sessionName;
    private final ReactorDispatcher dispatcher;
    private final Duration openTimeout;
    private final Disposable.Composite subscriptions;

    ReactorSession(Session session, SessionHandler handler, String sessionName, ReactorDispatcher dispatcher, Duration openTimeout) {
        super(new ServiceLogger(ReactorSession.class));

        this.session = session;
        this.handler = handler;
        this.sessionName = sessionName;
        this.dispatcher = dispatcher;
        this.openTimeout = openTimeout;

        this.subscriptions = Disposables.composite(
            this.handler.getEndpointStates().subscribe(
                this::notifyAndSetConnectionState,
                error -> notifyException(handler.getContext(error)),
                () -> notifyAndSetConnectionState(EndpointState.CLOSED)),
            this.handler.getErrors().subscribe(
                this::notifyException,
                error -> notifyException(handler.getContext(error)),
                () -> notifyAndSetConnectionState(EndpointState.CLOSED)));

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
    public String sessionName() {
        return sessionName;
    }

    @Override
    public Duration openTimeout() {
        return openTimeout;
    }

    @Override
    public Mono<AmqpLink> createSender(String linkName, Duration timeout) {
        return null;
    }

    @Override
    public Mono<AmqpLink> createReceiver(String linkName, Duration timeout) {
        return null;
    }

    @Override
    public boolean removeLink(String linkName) {
        return false;
    }
}
