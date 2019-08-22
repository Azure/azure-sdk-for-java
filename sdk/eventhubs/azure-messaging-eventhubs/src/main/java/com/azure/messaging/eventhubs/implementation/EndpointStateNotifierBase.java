// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.EndpointStateNotifier;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.EndpointState;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.io.Closeable;
import java.util.Objects;

abstract class EndpointStateNotifierBase implements EndpointStateNotifier, Closeable {
    private final ReplayProcessor<AmqpEndpointState> connectionStateProcessor = ReplayProcessor.cacheLastOrDefault(AmqpEndpointState.UNINITIALIZED);
    private final DirectProcessor<Throwable> errorContextProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpShutdownSignal> shutdownSignalProcessor = DirectProcessor.create();
    private final Disposable subscription;

    protected ClientLogger logger;
    private volatile AmqpEndpointState state;

    EndpointStateNotifierBase(ClientLogger logger) {
        Objects.requireNonNull(logger);

        this.logger = logger;
        this.subscription = connectionStateProcessor.subscribe(s -> this.state = s);
    }

    @Override
    public AmqpEndpointState getCurrentState() {
        return state;
    }

    @Override
    public Flux<Throwable> getErrors() {
        return errorContextProcessor;
    }

    @Override
    public Flux<AmqpEndpointState> getConnectionStates() {
        return connectionStateProcessor;
    }

    @Override
    public Flux<AmqpShutdownSignal> getShutdownSignals() {
        return shutdownSignalProcessor;
    }

    void notifyError(Throwable error) {
        Objects.requireNonNull(error);

        logger.error("Error occurred. {}", error.toString());
        errorContextProcessor.onNext(error);
    }

    void notifyShutdown(AmqpShutdownSignal shutdownSignal) {
        Objects.requireNonNull(shutdownSignal);

        logger.info("Notify shutdown signal: {}", shutdownSignal);
        shutdownSignalProcessor.onNext(shutdownSignal);
    }

    void notifyEndpointState(EndpointState endpointState) {
        Objects.requireNonNull(endpointState);

        logger.verbose("Connection state: {}", endpointState);
        final AmqpEndpointState state = getConnectionState(endpointState);
        connectionStateProcessor.onNext(state);
    }

    private static AmqpEndpointState getConnectionState(EndpointState state) {
        switch (state) {
            case ACTIVE:
                return AmqpEndpointState.ACTIVE;
            case UNINITIALIZED:
                return AmqpEndpointState.UNINITIALIZED;
            case CLOSED:
                return AmqpEndpointState.CLOSED;
            default:
                throw new UnsupportedOperationException("This endpoint state is not supported. State:" + state);
        }
    }

    @Override
    public void close() {
        subscription.dispose();
        connectionStateProcessor.onComplete();
        errorContextProcessor.onComplete();
        shutdownSignalProcessor.onComplete();
    }
}
