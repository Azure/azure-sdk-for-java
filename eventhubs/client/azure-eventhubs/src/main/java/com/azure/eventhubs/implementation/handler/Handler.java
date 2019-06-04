// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation.handler;

import com.azure.core.amqp.exception.ErrorContext;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.publisher.UnicastProcessor;

import java.io.Closeable;

public abstract class Handler extends BaseHandler implements Closeable {
    private final ReplayProcessor<EndpointState> endpointStateProcessor = ReplayProcessor.cacheLastOrDefault(EndpointState.UNINITIALIZED);
    private final UnicastProcessor<ErrorContext> errorContextProcessor = UnicastProcessor.create();
    private final FluxSink<EndpointState> endpointSink = endpointStateProcessor.sink();
    private final FluxSink<ErrorContext> errorSink = errorContextProcessor.sink();

    public Flux<EndpointState> getEndpointStates() {
        return endpointStateProcessor.distinct();
    }

    public Flux<ErrorContext> getErrors() {
        return errorContextProcessor;
    }

    void onNext(EndpointState state) {
        endpointSink.next(state);
    }

    void onNext(ErrorContext context) {
        errorSink.next(context);
    }

    @Override
    public void close() {
        onNext(EndpointState.CLOSED);

        endpointSink.complete();
        errorSink.complete();
    }
}
