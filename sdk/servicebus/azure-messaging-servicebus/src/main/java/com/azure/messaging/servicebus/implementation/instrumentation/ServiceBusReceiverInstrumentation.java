// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;

/**
 * Contains convenience methods to instrument specific calls with traces and metrics.
 */
public final class ServiceBusReceiverInstrumentation {
    private static final Symbol ENQUEUED_TIME_SYMBOL = Symbol.getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
    private final ServiceBusMeter meter;
    private final ServiceBusTracer tracer;
    private final ReceiverKind receiverKind;

    public ServiceBusReceiverInstrumentation(Tracer tracer, Meter meter, String fullyQualifiedName, String entityPath, String subscriptionName, ReceiverKind receiverKind) {
        this.tracer = new ServiceBusTracer(tracer, fullyQualifiedName, entityPath);
        this.meter = new ServiceBusMeter(meter, fullyQualifiedName, entityPath, subscriptionName);
        this.receiverKind = receiverKind;
    }

    /**
     * Creates subscription to report last settled sequence number. Call it in client constructor
     * and close along with client.
     */
    public AutoCloseable startTrackingSettlementSequenceNumber() {
        return meter.isSettlementEnabled() ? this.meter.trackSettlementSequenceNumber() : null;
    }

    /**
     * Checks if any receiver instrumentation is enabled
     */
    public boolean isEnabled() {
        return tracer.isEnabled() || meter.isSettlementEnabled() || meter.isConsumerLagEnabled();
    }

    /**
     * Checks if the instrumentation is created for processor client.
     *
     * @return true if the instrumentation is created to instrument message receive using processor client, false otherwise.
     */
    public boolean isProcessorInstrumentation() {
        return receiverKind == ReceiverKind.PROCESSOR;
    }

    /**
     * Checks if the instrumentation is created for Reactor async receiver client.
     *
     * @return true if the instrumentation is created to instrument message receive using Reactor receiver client, false otherwise.
     */
    public boolean isAsyncReceiverInstrumentation() {
        return receiverKind == ReceiverKind.ASYNC_RECEIVER;
    }

    /**
     * Instruments even processing. For Processor traces processMessage callback, for async receiver
     * traces subscriber call. Does not trace anything for sync receiver - use {@link ServiceBusTracer#traceSyncReceive(String, Flux)}
     * for sync receiver.
     * Reports consumer lag metric.
     */
    public Context startProcessInstrumentation(String name, Map<String, Object> applicationProperties,
        OffsetDateTime enqueuedTime, Context parent) {
        if (applicationProperties == null || (!tracer.isEnabled() && !meter.isConsumerLagEnabled())) {
            return parent;
        }

        Context span = (tracer.isEnabled() && receiverKind != ReceiverKind.SYNC_RECEIVER)
            ? tracer.startProcessSpan(name, applicationProperties, enqueuedTime, parent)
            : parent;

        // important to record metric after span is started
        meter.reportConsumerLag(enqueuedTime, span);

        return span;
    }

    public void instrumentProcess(ServiceBusReceivedMessage message, ReceiverKind caller, Function<ServiceBusReceivedMessage, Throwable> handleMessage) {
        if (receiverKind != caller || message == null) {
            handleMessage.apply(message);
            return;
        }

        Context span = startProcessInstrumentation("ServiceBus.process", message.getApplicationProperties(),
            message.getEnqueuedTime(), Context.NONE);
        ContextAccessor.setContext(message, span);
        wrap(span, message, handleMessage);
    }

    public void instrumentProcess(Message message, ReceiverKind caller, Function<Message, Throwable> handleMessage) {
        if (receiverKind != caller || message == null || message.getApplicationProperties() == null) {
            handleMessage.apply(message);
            return;
        }

        Context span = startProcessInstrumentation("ServiceBus.process", message.getApplicationProperties().getValue(),
            getEnqueuedTime(message), Context.NONE);
        //ContextAccessor.setContext(message, span);
        wrap(span, message, handleMessage);
    }

    /**
     * Instruments settlement calls. Creates a span for settlement call and reports settlement metrics.
     */
    public <T> Mono<T> instrumentSettlement(Mono<T> publisher, ServiceBusReceivedMessage message, Context messageContext, DispositionStatus status) {
        if (tracer.isEnabled() || meter.isSettlementEnabled()) {
            return Mono.defer(() -> {
                long startTime = Instant.now().toEpochMilli();
                Context span = tracer.startSpanWithLink(getSettlementSpanName(status), ServiceBusTracer.OperationName.SETTLE,
                    message, messageContext);
                return publisher
                    .doOnEach(signal -> {
                        meter.reportSettlement(startTime, message.getSequenceNumber(), status, signal.getThrowable(), false, span);
                        tracer.endSpan(signal.getThrowable(), span, null);
                    })
                    .doOnCancel(() -> {
                        meter.reportSettlement(startTime, message.getSequenceNumber(), status, null, true, span);
                        tracer.cancelSpan(span);

                    });
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

    private <T> void wrap(Context span, T message, Function<T, Throwable> handleMessage) {
        AutoCloseable scope = tracer.makeSpanCurrent(span);
        Throwable error = null;
        try {
            error = handleMessage.apply(message);
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            tracer.endSpan(error, span, scope);
        }
    }

    private OffsetDateTime getEnqueuedTime(Message message) {
        if (message.getMessageAnnotations() == null || message.getMessageAnnotations().getValue() == null) {
            return null;
        }

        Object date = message.getMessageAnnotations().getValue().get(ENQUEUED_TIME_SYMBOL);
        if (date instanceof Date) {
            return ((Date) date).toInstant().atOffset(ZoneOffset.UTC);
        }

        return null;
    }
}
