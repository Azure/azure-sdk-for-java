// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.apache.qpid.proton.amqp.Symbol;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.MESSAGE_ENQUEUED_TIME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;

public class ServiceBusReceiverTracer extends ServiceBusTracer {
    private static final Symbol ENQUEUED_TIME_UTC_ANNOTATION_NAME_SYMBOL = Symbol.valueOf(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());

    private static final AutoCloseable NOOP_AUTOCLOSEABLE = () -> {
    };

    private final boolean isSync;

    public ServiceBusReceiverTracer(Tracer tracer, String fullyQualifiedName, String entityPath, boolean isSync) {
        super(tracer, fullyQualifiedName, entityPath);
        this.isSync = isSync;
    }

    public Context startProcessSpan(String name, ServiceBusReceivedMessage message, Context parent) {
        if (message == null || tracer == null) {
            return parent;
        }
        return tracer.start(name, setAttributes(message, parent), ProcessKind.PROCESS);
    }

    public void reportReceiveSpan(String spanName, Instant startTime, Signal<ServiceBusReceivedMessage> signal) {
        if (tracer != null && (signal.isOnNext())) {
            ServiceBusReceivedMessage message = null;
            if (signal.hasValue()) {
                message = signal.get();
            }

            Context span = startSpanWithLink(spanName, message, Context.NONE.addData("span-start-time", startTime));
            endSpan(signal == null ? null : signal.getThrowable(), span, null);
        }
    }

    public Flux<ServiceBusReceivedMessage> reportSyncReceiverSpan(String name, Instant startTime, Flux<ServiceBusReceivedMessage> messages) {
        if (messages == null || tracer == null) {
            return messages;
        }

        List<ServiceBusReceivedMessage> messageList = new ArrayList<>();
        return messages
            .doOnEach(signal -> {
                if (signal.isOnNext() && signal.hasValue()) {
                    messageList.add(signal.get());
                } else if (signal.isOnComplete() || signal.isOnError()) {
                    Context span = startSpanWithLinks(name, messageList, Context.NONE.addData("span-start-time", startTime));
                    endSpan(signal.getThrowable(), span, null);
                }
            });
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
        return isSync;
    }

    private Context startSpanWithLinks(String name, List<ServiceBusReceivedMessage> batch, Context context) {
        Context spanBuilder = getBuilder(name, context);
        if (batch != null) {
            for (ServiceBusReceivedMessage message : batch) {
                addLink(message.getApplicationProperties(), message.getEnqueuedTime(), spanBuilder, Context.NONE);
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
        addLink(message.getApplicationProperties(), message.getEnqueuedTime(), spanBuilder, Context.NONE);

        // TODO: need to refactor tracing in core. Currently we use ProcessKind.SEND as
        // SpanKind.CLIENT
        return tracer.start(name, spanBuilder, ProcessKind.SEND);
    }

    private Context getParent(Map<String, Object> properties, Context context) {
        if (properties == null) {
            return context;
        }

        String traceparent = getTraceparent(properties);
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
