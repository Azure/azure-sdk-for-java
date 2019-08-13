// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.handler;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.publisher.UnicastProcessor;

import java.io.Closeable;

public abstract class Handler extends BaseHandler implements Closeable {
    private final ReplayProcessor<EndpointState> endpointStateProcessor = ReplayProcessor.cacheLastOrDefault(EndpointState.UNINITIALIZED);
    private final UnicastProcessor<Throwable> errorContextProcessor = UnicastProcessor.create();
    private final FluxSink<EndpointState> endpointSink = endpointStateProcessor.sink();
    private final FluxSink<Throwable> errorSink = errorContextProcessor.sink();
    private final String connectionId;
    private final String hostname;

    Handler(final String connectionId, final String hostname) {
        this.connectionId = connectionId;
        this.hostname = hostname;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getHostname() {
        return hostname;
    }

    public Flux<EndpointState> getEndpointStates() {
        return endpointStateProcessor.distinct();
    }

    public Flux<Throwable> getErrors() {
        return errorContextProcessor;
    }

    void onNext(EndpointState state) {
        endpointSink.next(state);
    }

    void onNext(Throwable context) {
        errorSink.next(context);
    }

    @Override
    public void close() {
        endpointSink.complete();
        errorSink.complete();
    }
}
