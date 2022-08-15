// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.ZoneOffset;
import java.util.Map;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.MESSAGE_ENQUEUED_TIME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;

public class ServiceBusReceiverTracer extends ServiceBusTracer {
    public static final String PROCESSING_ERROR_CONTEXT_KEY = "process-error";
    private static final AutoCloseable NOOP_AUTOCLOSEABLE = () -> {
    };

    private final boolean syncReceiver;
    public ServiceBusReceiverTracer(String fullyQualifiedName, String entityPath, boolean syncReceiver) {
        super(fullyQualifiedName, entityPath);
        this.syncReceiver = syncReceiver;
    }

    public ServiceBusReceiverTracer(Tracer tracer, String fullyQualifiedName, String entityPath, boolean syncReceiver) {
        super(tracer, fullyQualifiedName, entityPath);
        this.syncReceiver = syncReceiver;
    }

    public Context startProcessSpan(String name, ServiceBusReceivedMessage message, Context parent) {
        if (message == null || tracer == null) {
            return parent;
        }
        return tracer.start(name, setAttributes(message, parent), ProcessKind.PROCESS);
    }

    public void reportReceiveSpan(Signal<ServiceBusReceivedMessage> signal, String spanName) {
        if (tracer != null) {
            ServiceBusReceivedMessage message = null;
            if (signal != null && signal.hasValue()) {
                message = signal.get();
            }

            Context span = startSpanWithLink(spanName, message, Context.NONE);
            endSpan(signal == null ? null : signal.getThrowable(), span);
        }
    }

    public void reportReceiveSpan(Iterable<ServiceBusReceivedMessage> messages, String spanName, Throwable error) {
        if (tracer != null) {
            Context span = startSpanWithLinks(spanName, messages, Context.NONE);
            endSpan(error, span);
        }
    }

    public <T> Mono<T> traceMonoWithLink(Mono<T> publisher, ServiceBusReceivedMessage message, String spanName) {
        if (tracer == null) {
            return publisher;
        }

        return publisher
            .doOnEach(this::endSpan)
            .contextWrite(ctx -> ctx.put(REACTOR_PARENT_TRACE_CONTEXT_KEY, startSpanWithLink(spanName, message, Context.NONE)));
    }

    public <T> Mono<T> traceSettlement(Mono<T> publisher, ServiceBusReceivedMessage message, DispositionStatus status) {
        if (tracer == null) {
            return publisher;
        }

        return publisher
            .doOnEach(this::endSpan)
            .contextWrite(ctx -> ctx.put(REACTOR_PARENT_TRACE_CONTEXT_KEY, startSpanWithLink(getSettlementSpanName(status), message, Context.NONE)));
    }

    public AutoCloseable makeSpanCurrent(Context span) {
        return tracer == null ? NOOP_AUTOCLOSEABLE : tracer.makeSpanCurrent(span);
    }

    private static String getSettlementSpanName(DispositionStatus status) {
        switch (status) {
            case COMPLETED:
                return "ServiceBus.complete";
            case ABANDONED:
                return "ServiceBus.abandon";
            case DEFERRED:
                return "ServiceBus.defer";
            case SUSPENDED:
                return "ServiceBus.deadLetter";
            case RELEASED:
                return "ServiceBus.release";
            default:
                return "ServiceBus.unknown";
        }
    }

    public boolean isSync() {
        return syncReceiver;
    }

    private Context startSpanWithLinks(String name, Iterable<ServiceBusReceivedMessage> batch, Context context) {
        Context spanBuilder = getBuilder(name, context);
        if (batch != null) {
            for (ServiceBusReceivedMessage message : batch) {
                addLink(message.getApplicationProperties(), spanBuilder);
            }
        }

        // TODO: need to refactor tracing in core. Currently we use ProcessKind.SEND as
        // SpanKind.CLIENT
        return tracer.start(name, spanBuilder, ProcessKind.SEND);
    }

    private Context startSpanWithLink(String name, ServiceBusReceivedMessage message, Context parent) {
        if (message == null) {
            return parent;
        }
        Context spanBuilder = getBuilder(name, parent);
        addLink(message.getApplicationProperties(), spanBuilder);

        // TODO: need to refactor tracing in core. Currently we use ProcessKind.SEND as
        // SpanKind.CLIENT
        return tracer.start(name, spanBuilder, ProcessKind.SEND);
    }

    private Context getParent(Map<String, Object> properties, Context context) {
        if (properties == null) {
            return context;
        }

        Object diagnosticId = properties.get(DIAGNOSTIC_ID_KEY);
        if (diagnosticId == null) {
            diagnosticId = properties.get(TRACEPARENT_KEY);
        }

        String traceparent = diagnosticId == null ? null : diagnosticId.toString();

        return traceparent == null ? context : tracer.extractContext(traceparent, context);
    }

    private Context setAttributes(ServiceBusReceivedMessage message, Context context) {
        if (message.getEnqueuedTime() != null) {
            context = context.addData(MESSAGE_ENQUEUED_TIME, message.getEnqueuedTime().toInstant().atOffset(ZoneOffset.UTC).toEpochSecond());
        }

        context = getParent(message.getApplicationProperties(), context);

        return context
            .addData(Tracer.ENTITY_PATH_KEY, entityPath)
            .addData(HOST_NAME_KEY, fullyQualifiedName)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
    }
}
