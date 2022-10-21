// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.ServiceBusTracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

class ServiceBusSenderTracer extends ServiceBusTracer {
    ServiceBusSenderTracer(Tracer tracer, String fullyQualifiedName, String entityPath) {
        super(tracer, fullyQualifiedName, entityPath);
    }

    <T> Mono<T> traceMonoWithLink(Mono<T> publisher, ServiceBusMessage message, String spanName) {
        if (tracer == null) {
            return publisher;
        }

        return publisher
            .doOnEach(this::endSpan)
            .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY, startSpanWithLink(spanName, message, Context.NONE)));
    }

    <T> Mono<T> traceMonoWithLinks(Mono<T> publisher, ServiceBusMessageBatch batch, String spanName) {
        if (tracer == null) {
            return publisher;
        }

        return publisher
            .doOnEach(this::endSpan)
            .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY, startSpanWithLinks(spanName, batch, Context.NONE)));
    }

    <T> Flux<T> traceFluxWithLinks(Flux<T> publisher, ServiceBusMessageBatch batch, String spanName) {
        if (tracer == null) {
            return publisher;
        }

        return publisher
            .doOnEach(this::endSpan)
            .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY, startSpanWithLinks(spanName, batch, Context.NONE)));
    }

    /**
     * Used in ServiceBusMessageBatch.tryAddMessage() to start tracing for to-be-sent out messages.
     */
    void createMessageSpan(ServiceBusMessage serviceBusMessage) {
        Context messageContext = serviceBusMessage.getContext();
        if (tracer == null || messageContext == null || messageContext.getData(SPAN_CONTEXT_KEY).isPresent()) {
            // if message has context (in case of retries), don't start a message span or add a new context
            return;
        }

        String traceparent = getTraceparent(serviceBusMessage.getApplicationProperties());
        if (traceparent != null) {
            // if message has context (in case of retries) or if user supplied it, don't start a message span or add a new context
            return;
        }

        // Starting the span makes the sampling decision (nothing is logged at this time)
        Context newMessageContext = setAttributes(messageContext);

        Context eventSpanContext = tracer.start("ServiceBus.message", newMessageContext, ProcessKind.MESSAGE);
        Optional<Object> traceparentOpt = eventSpanContext.getData(DIAGNOSTIC_ID_KEY);

        if (traceparentOpt.isPresent()) {
            serviceBusMessage.getApplicationProperties().put(DIAGNOSTIC_ID_KEY, traceparentOpt.get().toString());
            serviceBusMessage.getApplicationProperties().put(TRACEPARENT_KEY, traceparentOpt.get().toString());

            endSpan(null, eventSpanContext, null);

            Optional<Object> spanContext = eventSpanContext.getData(SPAN_CONTEXT_KEY);
            if (spanContext.isPresent()) {
                serviceBusMessage.addContext(SPAN_CONTEXT_KEY, spanContext.get());
            }
        }
    }

    private Context startSpanWithLink(String name, ServiceBusMessage message, Context context) {
        Context spanBuilder = getBuilder(name, context);
        addLink(message, spanBuilder);

        return tracer.start(name, spanBuilder, ProcessKind.SEND);
    }

    private void addLink(ServiceBusMessage message, Context spanBuilder) {
        if (message == null) {
            return;
        }

        String traceparent = getTraceparent(message.getApplicationProperties());

        if (traceparent == null) {
            createMessageSpan(message);
        }

        addLink(message.getApplicationProperties(), null, spanBuilder, Context.NONE);
    }

    private Context startSpanWithLinks(String name, ServiceBusMessageBatch batch, Context context) {
        Context spanBuilder = getBuilder(name, context);
        if (batch != null) {
            for (ServiceBusMessage message : batch.getMessages()) {
                addLink(message, spanBuilder);
            }
        }

        return tracer.start(name, spanBuilder, ProcessKind.SEND);
    }
}
