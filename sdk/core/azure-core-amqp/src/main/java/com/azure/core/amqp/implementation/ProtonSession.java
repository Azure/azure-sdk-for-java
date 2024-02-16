// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.amqp.implementation.ClientConstants.SESSION_NAME_KEY;
import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

final class ProtonSession {
    private static final String DISPOSED_MESSAGE_FORMAT = "Cannot create %s in a closed session.";
    private final AtomicReference<State> state = new AtomicReference<>(State.EMPTY);
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final Sinks.Empty<Void> openAwaiter = Sinks.empty();
    private final Connection connection;
    private final ReactorProvider reactorProvider;
    private final SessionHandler handler;
    private final ClientLogger logger;

    ProtonSession(String fullyQualifiedNamespace, String connectionId, Connection connection,
        ReactorHandlerProvider handlerProvider, ReactorProvider reactorProvider, String sessionName,
        Duration openTimeout, ClientLogger logger) {
        this.connection = Objects.requireNonNull(connection, "'connection' cannot be null.");
        this.reactorProvider = Objects.requireNonNull(reactorProvider, "'reactorProvider' cannot be null.");
        Objects.requireNonNull(handlerProvider, "'handlerProvider' cannot be null.");
        this.handler = handlerProvider.createSessionHandler(connectionId, fullyQualifiedNamespace, sessionName, openTimeout);
        this.logger = Objects.requireNonNull(logger, "'logger' cannot be null.");
    }

    String getName() {
        return handler.getSessionName();
    }

    String getHostname() {
        return handler.getHostname();
    }

    String getConnectionId() {
        return handler.getConnectionId();
    }

    AmqpErrorContext getErrorContext() {
        return handler.getErrorContext();
    }

    Flux<EndpointState> getEndpointStates() {
        return handler.getEndpointStates();
    }

    Mono<Void> open() {
        if (opened.getAndSet(true)) {
            return openAwaiter.asMono();
        }
        try {
            reactorProvider.getReactorDispatcher().invoke(() -> {
                final Session session = connection.session();
                BaseHandler.setHandler(session, handler);
                session.open();
                logger.atInfo().addKeyValue(SESSION_NAME_KEY, handler.getSessionName()).log("session open scheduled.");

                final State s = state.compareAndExchange(State.EMPTY, new State(session));
                if (s == State.EMPTY) {
                    openAwaiter.emitEmpty(FAIL_FAST);
                } else {
                    session.close();
                    if (s == State.DISPOSED) {
                        openAwaiter.emitError(
                            retriableAmqpError(null, "session is disposed.", null), FAIL_FAST);
                    } else {
                        openAwaiter.emitError(new IllegalStateException("session is already opened."), FAIL_FAST);
                    }
                }
            });
        } catch (Exception e) {
            if (e instanceof IOException | e instanceof RejectedExecutionException) {
                openAwaiter.emitError(
                    retriableAmqpError(null, "connection-reactor is disposed.", e), FAIL_FAST);
            } else {
                openAwaiter.emitError(e, FAIL_FAST);
            }
        }
        return openAwaiter.asMono();
    }

    Session get(String childName) {
        final State s = state.get();
        if (s == State.EMPTY) {
            throw logger.logExceptionAsError(new IllegalStateException("session has not been opened."));
        }
        if (s == State.DISPOSED) {
            throw logger.logExceptionAsError(
                retriableAmqpError(null, String.format(DISPOSED_MESSAGE_FORMAT, childName), null));
        }
        final Session session = s.get();
        return session;
    }

    Sender sender(String name) {
        return get("sender link").sender(name);
    }

    Receiver receiver(String name) {
        return get("receive link").receiver(name);
    }

    void beginClose(ErrorCondition errorCondition) {
        final State s = state.getAndSet(State.DISPOSED);
        if (s == State.EMPTY || s == State.DISPOSED) {
            return;
        }
        final Session session = s.get();
        if (session.getLocalState() != EndpointState.CLOSED) {
            session.close();

            if (errorCondition != null && session.getCondition() == null) {
                session.setCondition(errorCondition);
            }
        }
    }

    void endClose() {
        handler.close();
    }

    private AmqpException retriableAmqpError(AmqpErrorCondition condition, String message, Throwable cause) {
        // The call sites uses this method to translate a session unavailability "event" (session disposed, or
        // connection being closed) to a retriable error. While the "event" is transient, resolving it (e.g. by
        // acquiring a new connection) needs to be done not by the parent 'AmqpConnection' but by the downstream layer.
        // E.g., the downstream Consumer, Producer Client that has access to the chain to propagate retry request to
        // top level connection-cache (or v1 connection-processor).
        return new AmqpException(true, condition, message, cause, handler.getErrorContext());
    }

    private static final class State {
        private static final State EMPTY = new State();
        private static final State DISPOSED = new State();
        private final Session session;

        private State() {
            // Ctr for the EMPTY and DISPOSED state.
            this.session = null;
        }

        private State(Session session) {
            this.session = Objects.requireNonNull(session, "'session' cannot be null.");
        }

        private Session get() {
            assert this != EMPTY;
            return this.session;
        }
    }
}
