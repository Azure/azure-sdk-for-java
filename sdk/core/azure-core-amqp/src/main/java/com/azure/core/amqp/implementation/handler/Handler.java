// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for all proton-j handlers.
 */
public abstract class Handler extends BaseHandler implements Closeable {
    private final AtomicBoolean isTerminal = new AtomicBoolean();
    private final ReplayProcessor<EndpointState> endpointStateProcessor =
        ReplayProcessor.cacheLastOrDefault(EndpointState.UNINITIALIZED);
    private final FluxSink<EndpointState> endpointSink = endpointStateProcessor.sink();
    private final String connectionId;
    private final String hostname;

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
     * Usually of the form {@literal "<your-namespace>.service.windows.net"} but can change if the messages are
     * brokered through an intermediary.
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
        return endpointStateProcessor.distinct();
    }

    void onNext(EndpointState state) {
        endpointSink.next(state);
    }

    void onError(Throwable error) {
        if (isTerminal.getAndSet(true)) {
            return;
        }

        endpointSink.next(EndpointState.CLOSED);
        endpointSink.error(error);
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

        endpointSink.next(EndpointState.CLOSED);
        endpointSink.complete();
    }
}
