// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer.REACTOR_PARENT_TRACE_CONTEXT_KEY;

/**
 * Contains convenience methods to instrument specific calls with traces and metrics.
 */
public class ServiceBusReceiverInstrumentation {
    private final ServiceBusMeter meter;
    private final ServiceBusTracer tracer;

    private final boolean isSync;

    public ServiceBusReceiverInstrumentation(Tracer tracer, Meter meter, String fullyQualifiedName, String entityPath, String subscriptionName, boolean isSync) {
        this.tracer = new ServiceBusTracer(tracer, fullyQualifiedName, entityPath);
        this.meter = new ServiceBusMeter(meter, fullyQualifiedName, entityPath, subscriptionName);
        this.isSync = isSync;
    }

    /**
     * Creates subscription to report last settled sequence number. Call it in client constructor
     * and close along with client.
     */
    public AutoCloseable startTrackingSettlementSequenceNumber() {
        return meter.isSettlementEnabled() ? this.meter.trackSettlementSequenceNumber() : null;
    }

    /**
     * Instruments even processing. For Processor traces processMessage callback, for async receiver
     * traces subscriber call. Does not trace anything for sync receiver - use {@link ServiceBusTracer#traceSyncReceive(String, Flux)}
     * for sync receiver.
     * Reports consumer lag metric.
     */
    public Context instrumentProcess(String name, ServiceBusReceivedMessage message, Context parent) {
        if (!tracer.isEnabled() && !meter.isConsumerLagEnabled()) {
            return parent;
        }

        Context span = (tracer.isEnabled() && !isSync) ? tracer.startProcessSpan(name, message, parent) : parent;
        meter.reportConsumerLag(message.getEnqueuedTime(), span);

        return span;
    }

    /**
     * Instruments settlement calls. Creates a span for settlement call and reports settlement metrics.
     */
    public <T> Mono<T> instrumentSettlement(Mono<T> publisher, ServiceBusReceivedMessage message, Context messageContext, DispositionStatus status) {
        if (tracer.isEnabled() || meter.isSettlementEnabled()) {
            AtomicLong startTime = new AtomicLong();
            return publisher
                .doOnEach(signal -> {
                    Context span = signal.getContextView().getOrDefault(REACTOR_PARENT_TRACE_CONTEXT_KEY, Context.NONE);
                    meter.reportSettlement(startTime.get(), message.getSequenceNumber(), status, signal.getThrowable(), span);
                    tracer.endSpan(signal.getThrowable(), span, null);
                })
                .contextWrite(ctx -> {
                    startTime.set(Instant.now().toEpochMilli());
                    return ctx.put(REACTOR_PARENT_TRACE_CONTEXT_KEY, tracer.startSpanWithLink(getSettlementSpanName(status), message,
                        messageContext, Context.NONE));
                });
        }

        return publisher;
    }

    public ServiceBusTracer getTracer() {
        return tracer;
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
}
