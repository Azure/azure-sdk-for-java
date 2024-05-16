// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addSignalTypeAndResult;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.createContextWithConnectionId;

/**
 * Base class for all proton-j handlers.
 */
public abstract class Handler extends BaseHandler implements Closeable {
    // The flux streaming state of the amqp endpoint (connection, session, link) from this handler receives events.
    private final Sinks.Many<EndpointState> endpointStates
        = Sinks.many().replay().latestOrDefault(EndpointState.UNINITIALIZED);
    // The flag indicating if the endpointStates Flux reached terminal state (error-ed or completed).
    private final AtomicBoolean isTerminal = new AtomicBoolean();
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
        this.connectionId = Objects.requireNonNull(connectionId, "'connectionId' cannot be null.");
        this.hostname = Objects.requireNonNull(hostname, "'hostname' cannot be null.");
        this.logger = new ClientLogger(getClass(), createContextWithConnectionId(connectionId));
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
        if (isTerminal.get()) {
            return;
        }

        endpointStates.emitNext(state, (signalType, emitResult) -> {
            if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                addSignalTypeAndResult(logger.atVerbose(), signalType, emitResult)
                    .log("Could not emit endpoint state. Non-serial access. Retrying.");

                return true;
            } else {
                addSignalTypeAndResult(logger.atVerbose(), signalType, emitResult)
                    .log("Could not emit endpoint state.");

                return false;
            }
        });
    }

    /**
     * Emits an error if the handler has not reached a terminal state already.
     *
     * @param error The error to emit.
     */
    public void onError(Throwable error) {
        if (isTerminal.getAndSet(true)) {
            return;
        }

        endpointStates.emitError(error, (signalType, emitResult) -> {
            if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                addSignalTypeAndResult(logger.atVerbose(), signalType, emitResult)
                    .log("Could not emit error. Non-serial access. Retrying.", error);

                return true;
            } else {
                addSignalTypeAndResult(logger.atVerbose(), signalType, emitResult).log("Could not emit error.", error);

                return false;
            }
        });
    }

    /**
     * Changes the endpoint to {@link EndpointState#CLOSED} and completes the stream of {@link #getEndpointStates()
     * endpoint states}.
     */
    @Override
    public void close() {
        if (isTerminal.getAndSet(true)) {
            return;
        }

        // This is fine in the case that someone called onNext(EndpointState.CLOSED) and then called handler.close().
        // We want to ensure that the next endpoint subscriber does not believe the handler is alive still.
        endpointStates.emitNext(EndpointState.CLOSED, (signalType, emitResult) -> {
            if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                addSignalTypeAndResult(logger.atInfo(), signalType, emitResult)
                    .log("Could not emit closed endpoint state. Non-serial access. Retrying.");

                return true;
            } else {
                addSignalTypeAndResult(logger.atInfo(), signalType, emitResult)
                    .log("Could not emit closed endpoint state.");

                return false;
            }
        });

        endpointStates.emitComplete((signalType, emitResult) -> {
            if (emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                addSignalTypeAndResult(logger.atInfo(), signalType, emitResult)
                    .log("Could not emit complete. Non-serial access. Retrying.");

                return true;
            } else {
                addSignalTypeAndResult(logger.atInfo(), signalType, emitResult).log("Could not emit complete.");

                return false;
            }
        });
    }
}
