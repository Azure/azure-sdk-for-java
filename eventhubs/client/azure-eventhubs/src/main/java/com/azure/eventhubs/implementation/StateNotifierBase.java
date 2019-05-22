// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.ConnectionState;
import com.azure.core.amqp.ShutdownSignal;
import com.azure.core.amqp.StateNotifier;
import com.azure.core.amqp.exception.ErrorContext;
import com.azure.core.implementation.logging.ServiceLogger;
import org.apache.qpid.proton.engine.EndpointState;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.io.Closeable;

abstract class StateNotifierBase implements StateNotifier, Closeable {
    private final ReplayProcessor<ConnectionState> connectionStateProcessor = ReplayProcessor.cacheLastOrDefault(ConnectionState.UNINITIALIZED);
    private final DirectProcessor<ErrorContext> errorContextProcessor = DirectProcessor.create();
    private final DirectProcessor<ShutdownSignal> shutdownSignalProcessor = DirectProcessor.create();
    private final Disposable subscription;

    protected ServiceLogger logger;
    private volatile ConnectionState state;

    StateNotifierBase(ServiceLogger logger) {
        this.logger = logger;
        this.subscription = connectionStateProcessor.subscribe(s -> this.state = s);
    }

    @Override
    public ConnectionState getCurrentState() {
        return state;
    }

    @Override
    public Flux<ErrorContext> getErrors() {
        return errorContextProcessor;
    }

    @Override
    public Flux<ConnectionState> getConnectionStates() {
        return connectionStateProcessor;
    }

    @Override
    public Flux<ShutdownSignal> getShutdownSignals() {
        return shutdownSignalProcessor;
    }

    void notifyException(ErrorContext error) {
        errorContextProcessor.onNext(error);
    }

    void notifyShutdown(ShutdownSignal shutdownSignal) {
        shutdownSignalProcessor.onNext(shutdownSignal);
    }

    void notifyAndSetConnectionState(EndpointState endpointState) {
        logger.asInformational().log("Connection state: {}", endpointState);
        final ConnectionState state = getConnectionState(endpointState);
        connectionStateProcessor.onNext(state);
    }

    private static ConnectionState getConnectionState(EndpointState state) {
        switch (state) {
            case ACTIVE:
                return ConnectionState.ACTIVE;
            case UNINITIALIZED:
                return ConnectionState.UNINITIALIZED;
            case CLOSED:
                return ConnectionState.CLOSED;
            default:
                throw new UnsupportedOperationException("This endpoint state is not supported. State:" + state);
        }
    }

    @Override
    public void close() {
        subscription.dispose();
        connectionStateProcessor.onComplete();
        errorContextProcessor.onComplete();
    }
}
