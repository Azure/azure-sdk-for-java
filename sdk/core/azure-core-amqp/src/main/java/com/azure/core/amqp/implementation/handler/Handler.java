// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.createContextWithConnectionId;

/**
 * Base class for all proton-j handlers.
 */
public abstract class Handler extends BaseHandler implements Closeable {
    // The flux streaming state of the amqp endpoint (connection, session, link) from this handler receives events.
    private final Sinks.Many<EndpointState> endpointStates
        = Sinks.many().replay().latestOrDefault(EndpointState.UNINITIALIZED);
    // Guard signal admission and drain ownership without holding a lock across subscriber callbacks.
    private final Queue<Runnable> pendingSignals = new ArrayDeque<>();
    private boolean emitting;
    private boolean terminalQueued;
    private final String id;
    private final String connectionId;
    private final String hostname;

    final ClientLogger logger;

    /**
     * Creates an instance with the parameters.
     *
     * @param connectionId Identifier for the connection.
     * @param hostname Hostname of the connection. This could be the DNS hostname or the IP address of the
     *     connection. Usually of the form {@literal "<your-namespace>.service.windows.net"} but can change if the
     *     messages are brokered through an intermediary.
     *
     * @throws NullPointerException if {@code connectionId} or {@code hostname} is null.
     */
    Handler(final String connectionId, final String hostname) {
        this.id = StringUtil.getRandomString("H");
        this.connectionId = Objects.requireNonNull(connectionId, "'connectionId' cannot be null.");
        this.hostname = Objects.requireNonNull(hostname, "'hostname' cannot be null.");
        this.logger = new ClientLogger(getClass(), createContextWithConnectionId(connectionId));
    }

    /**
     * Gets the id of the handler.
     *
     * @return The handler id.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the connection id.
     *
     * @return The connection id.
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Gets the hostname of the AMQP connection. This could be the DNS hostname or the IP address of the connection.
     * Usually of the form {@literal "<your-namespace>.service.windows.net"} but can change if the messages are brokered
     * through an intermediary.
     *
     * @return Gets the hostname of the AMQP connection.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Gets the endpoint states of the handler.
     *
     * @return The endpoint states of the handler.
     */
    public Flux<EndpointState> getEndpointStates() {
        // In previous incarnations, we used .distinct(). It hashed all the previous values and would only push values
        // that were not seen yet. What we want is only to push endpoint states that are unique from the previous one.
        return endpointStates.asFlux().distinctUntilChanged();
    }

    /**
     * Emits the next endpoint. If the previous endpoint was emitted, it is skipped. If the handler is closed, the
     * endpoint state is not emitted.
     *
     * @param state The next endpoint state to emit.
     */
    void onNext(EndpointState state) {
        Objects.requireNonNull(state, "'state' cannot be null.");
        emit(() -> endpointStates.emitNext(state, Sinks.EmitFailureHandler.FAIL_FAST), false);
    }

    /**
     * Emits an error if the handler has not reached a terminal state already.
     *
     * @param error The error to emit.
     */
    public void onError(Throwable error) {
        Objects.requireNonNull(error, "'error' cannot be null.");
        emit(() -> endpointStates.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST), true);
    }

    /**
     * Changes the endpoint to {@link EndpointState#CLOSED} and completes the stream of {@link #getEndpointStates()
     * endpoint states}. If a notification is in progress, that emitter delivers the close signals after it returns.
     */
    @Override
    public void close() {
        emit(() -> {
            // Replay CLOSED to late subscribers as well as notifying current subscribers.
            endpointStates.emitNext(EndpointState.CLOSED, Sinks.EmitFailureHandler.FAIL_FAST);
            endpointStates.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        }, true);
    }

    private void emit(Runnable signal, boolean terminal) {
        synchronized (pendingSignals) {
            if (terminalQueued) {
                return;
            }

            terminalQueued = terminal;
            if (emitting) {
                pendingSignals.add(signal);
                return;
            }
            emitting = true;
        }

        // The current emitter delivers any signals queued by concurrent or reentrant callers.
        Runnable next = signal;
        for (;;) {
            next.run();
            synchronized (pendingSignals) {
                next = pendingSignals.poll();
                if (next == null) {
                    emitting = false;
                    return;
                }
            }
        }
    }
}
