// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracingLink;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

/**
 * Tracing helper.
 */
public final class ServiceBusTracer {
    private static final AutoCloseable NOOP_CLOSEABLE = () -> {
    };
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusTracer.class);
    private static final String TRACEPARENT_KEY = "traceparent";
    private static final String MESSAGING_SYSTEM_ATTRIBUTE_NAME = "messaging.system";
    public static final String MESSAGE_ENQUEUED_TIME_ATTRIBUTE_NAME = "messaging.servicebus.message.enqueued_time";
    public static final String MESSAGE_BATCH_SIZE_ATTRIBUTE_NAME = "messaging.batch.message_count";

    private static final String MESSAGING_OPERATION_ATTRIBUTE_NAME = "messaging.operation";
    private static final boolean IS_TRACING_DISABLED = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TRACING_DISABLED, false);
    private final Tracer tracer;
    private final String fullyQualifiedName;
    private final String entityPath;

    public ServiceBusTracer(Tracer tracer, String fullyQualifiedName, String entityPath) {
        this.tracer = IS_TRACING_DISABLED ? null : tracer;
        this.fullyQualifiedName = Objects.requireNonNull(fullyQualifiedName, "'fullyQualifiedName' cannot be null");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null");
    }

    /**
     * Checks if tracing is enabled.
     */
    public boolean isEnabled() {
        return tracer != null && tracer.isEnabled();
    }

    /**
     * Makes span in provided context (if any) current. Caller is responsible to close the returned scope.
     */
    public AutoCloseable makeSpanCurrent(Context span) {
        return isEnabled() ? tracer.makeSpanCurrent(span) : NOOP_CLOSEABLE;
    }

    /**
     * Traces arbitrary mono. No special send or receive semantics is applied.
     */
    public <T> Mono<T> traceMono(String spanName, Mono<T> publisher) {
        return traceMono(spanName, publisher, Context.NONE);
    }

    /**
     * Traces message renew lock
     */
    public <T> Mono<T> traceRenewMessageLock(Mono<T> publisher, ServiceBusReceivedMessage message) {
        if (isEnabled()) {
            return Mono.defer(() -> {
                Context span = startSpanWithLink("ServiceBus.renewMessageLock", null, message, Context.NONE);
                return publisher.doOnEach(signal -> {
                    if (signal.isOnComplete() || signal.isOnError()) {
                        endSpan(signal.getThrowable(), span, null);
                    }
                })
                .doOnCancel(() -> cancelSpan(span));
            });
        }

        return publisher;
    }

    /**
     * Traces arbitrary mono that operates with sent message as input, e.g. schedule. No special send or receive semantics is applied.
     */
    public <T> Mono<T> traceScheduleMono(String spanName, Mono<T> publisher, ServiceBusMessage message, Context messageContext) {
        return traceMonoWithLink(spanName, OperationName.PUBLISH, publisher, message, messageContext);
    }

    /**
     * Traces arbitrary mono that operates with batch of sent message as input, e.g. schedule. No special send or receive semantics is applied.
     */
    public <T> Flux<T> traceScheduleFlux(String spanName, Flux<T> publisher, List<ServiceBusMessage> batch) {
        if (isEnabled()) {
            return Flux.defer(() -> {
                Context span = startSpanWithLinks(spanName, OperationName.PUBLISH, batch, Context.NONE);
                return publisher
                    .doOnEach(signal -> {
                        if (signal.isOnComplete() || signal.isOnError()) {
                            endSpan(signal.getThrowable(), span, null);
                        }
                    })
                    .doOnCancel(() -> cancelSpan(span));
            });
        }
        return publisher;
    }

    /**
     * Ends span and scope.
     */
    public void endSpan(Throwable throwable, Context span, AutoCloseable scope) {
        if (isEnabled()) {
            closeScope(scope);

            String errorCondition = null;
            if (throwable instanceof AmqpException) {
                AmqpException exception = (AmqpException) throwable;
                if (exception.getErrorCondition() != null) {
                    errorCondition = exception.getErrorCondition().getErrorCondition();
                }
            }
            tracer.end(errorCondition, throwable, span);
        }
    }

    public void closeScope(AutoCloseable scope) {
        if (scope != null) {
            try {
                scope.close();
            } catch (Exception e) {
                LOGGER.warning("Can't close scope", e);
            }
        }
    }

    /**
     * Used in ServiceBusMessageBatch.tryAddMessage() to start tracing for to-be-sent out messages.
     */
    public void reportMessageSpan(ServiceBusMessage serviceBusMessage) {
        if (!isEnabled()) {
            return;
        }

        Context messageContext = ContextAccessor.getContext(serviceBusMessage);
        if (messageContext == null || messageContext.getData(SPAN_CONTEXT_KEY).isPresent()) {
            // if message has context (in case of retries), don't start a message span or add a new context
            return;
        }

        Map<String, Object> applicationProperties = serviceBusMessage.getApplicationProperties();
        String traceparent = getTraceparent(applicationProperties);
        if (traceparent != null) {
            // if message has context (in case of retries) or if user supplied it, don't start a message span or add a new context
            return;
        }

        // Starting the span makes the sampling decision (nothing is logged at this time)
        StartSpanOptions startOptions = createStartOption(SpanKind.PRODUCER, null);

        Context eventSpanContext = tracer.start("ServiceBus.message", startOptions, messageContext);
        tracer.injectContext((key, value) -> {
            applicationProperties.put(key, value);
            if (TRACEPARENT_KEY.equals(key)) {
                applicationProperties.put(DIAGNOSTIC_ID_KEY, value);
            }
        }, eventSpanContext);

        tracer.end(null, null, eventSpanContext);

        Optional<Object> spanContext = eventSpanContext.getData(SPAN_CONTEXT_KEY);
        if (spanContext.isPresent()) {
            serviceBusMessage.addContext(SPAN_CONTEXT_KEY, spanContext.get());
        }
    }

    /**
     * Instruments peek or receiveDeferred that return a single message. Creates a single span, does not report any metrics
     */
    public Mono<ServiceBusReceivedMessage> traceManagementReceive(String spanName, Mono<ServiceBusReceivedMessage> publisher) {
        if (isEnabled()) {
            final StartSpanOptions startOptions = createStartOption(SpanKind.CLIENT, OperationName.RECEIVE);
            return publisher
                .doOnEach(signal -> {
                    if (signal.hasValue()) {
                        ServiceBusReceivedMessage message = signal.get();
                        if (message != null) {
                            Context messageContext = ContextAccessor.getContext(message);
                            startOptions.addLink(createLink(message.getApplicationProperties(), message.getEnqueuedTime(), messageContext));
                        }
                    }

                    if (signal.isOnComplete() || signal.isOnError()) {
                        Context span = tracer.start(spanName, startOptions, Context.NONE);
                        tracer.end(null, signal.getThrowable(), span);
                    }
                })
                .doOnCancel(() ->  {
                    cancelSpan(tracer.start(spanName, startOptions, Context.NONE));
                })
                .doOnSubscribe(s -> startOptions.setStartTimestamp(Instant.now()));
        }
        return publisher;
    }


    /**
     * Traces receive, peek, receiveDeferred that return a flux of messages, but has a limited lifetime - such as sync receive case or peek many
     * or receive deferred.
     *
     * Don't use it for async receive when Flux has unknown lifetime!!!
     *
     * Creates a single span with links to each message being received.
     */
    public Flux<ServiceBusReceivedMessage> traceSyncReceive(String spanName, Flux<ServiceBusReceivedMessage> messages) {
        if (isEnabled()) {
            return Flux.defer(() -> {
                StartSpanOptions startOptions = createStartOption(SpanKind.CLIENT, OperationName.RECEIVE)
                    .setStartTimestamp(Instant.now());
                return messages
                    .doOnEach(signal -> {
                        if (signal.hasValue()) {
                            ServiceBusReceivedMessage message = signal.get();
                            if (message != null) {
                                TracingLink link = createLink(message.getApplicationProperties(), message.getEnqueuedTime(), Context.NONE);
                                startOptions.addLink(link);
                                ContextAccessor.setContext(message, link.getContext());
                            }
                        } else if (signal.isOnComplete() || signal.isOnError()) {
                            int batchSize = startOptions.getLinks() == null ? 0 : startOptions.getLinks().size();
                            startOptions.setAttribute(MESSAGE_BATCH_SIZE_ATTRIBUTE_NAME, batchSize);
                            Context span = tracer.start(spanName, startOptions, Context.NONE);
                            tracer.end(null, signal.getThrowable(), span);
                        }
                    })
                    .doOnCancel(() -> cancelSpan(tracer.start(spanName, startOptions, Context.NONE)));
            });
        }
        return messages;
    }

    Context startSpanWithLinks(String spanName, OperationName operationName, List<ServiceBusMessage> batch, Context parent) {
        if (isEnabled() && batch != null) {
            StartSpanOptions startOptions = createStartOption(SpanKind.CLIENT, operationName);
            startOptions.setAttribute(MESSAGE_BATCH_SIZE_ATTRIBUTE_NAME, batch.size());
            for (ServiceBusMessage message : batch) {
                Context messageContext = ContextAccessor.getContext(message);
                startOptions.addLink(createLink(message.getApplicationProperties(), null, messageContext));
            }

            return tracer.start(spanName, startOptions, parent);
        }

        return parent;
    }

    Context startSpanWithLink(String spanName, OperationName operationName, ServiceBusReceivedMessage message, Context parent) {
        if (isEnabled()) {
            StartSpanOptions startOptions = createStartOption(SpanKind.CLIENT, operationName);
            if (message != null) {
                Context messageContext = ContextAccessor.getContext(message);
                startOptions.addLink(createLink(message.getApplicationProperties(), message.getEnqueuedTime(), messageContext));
            } else {
                startOptions.addLink(createLink(null, null, parent));
            }
            return tracer.start(spanName, startOptions, parent);
        }

        return parent;
    }

    /**
     * Starts span. Used by ServiceBus*Instrumentations.
     */
    Context startProcessSpan(String spanName, Map<String, Object> applicationProperties, OffsetDateTime enqueuedTime, Context parent) {
        if (isEnabled() && applicationProperties != null) {
            StartSpanOptions startOptions = createStartOption(SpanKind.CONSUMER, OperationName.PROCESS)
                .setRemoteParent(extractContext(applicationProperties));

            if (enqueuedTime != null) {
                startOptions.setAttribute(MESSAGE_ENQUEUED_TIME_ATTRIBUTE_NAME, enqueuedTime.toEpochSecond());
            }

            return tracer.start(spanName, startOptions, parent);
        }

        return parent;
    }

    private TracingLink createLink(Map<String, Object> applicationProperties, OffsetDateTime enqueuedTime, Context eventContext) {
        Context link;
        // in case of send, we have remote SpanContext context in span_context
        if (eventContext.getData(SPAN_CONTEXT_KEY).isPresent()) {
            link = eventContext;
        } else {
            link = extractContext(applicationProperties);
        }

        Map<String, Object> linkAttributes = null;
        if (enqueuedTime != null) {
            linkAttributes = Collections.singletonMap(MESSAGE_ENQUEUED_TIME_ATTRIBUTE_NAME, enqueuedTime.toEpochSecond());
        }

        return new TracingLink(link, linkAttributes);
    }

    private Context extractContext(Map<String, Object> applicationProperties) {
        if (applicationProperties == null) {
            return Context.NONE;
        }

        return tracer.extractContext(key ->  {
            if (TRACEPARENT_KEY.equals(key)) {
                return getTraceparent(applicationProperties);
            } else {
                Object value = applicationProperties.get(key);
                if (value != null) {
                    return value.toString();
                }
            }
            return null;
        });
    }

    private static String getTraceparent(Map<String, Object> applicationProperties) {
        Object diagnosticId = applicationProperties.get(DIAGNOSTIC_ID_KEY);
        if (diagnosticId == null) {
            diagnosticId = applicationProperties.get(TRACEPARENT_KEY);
        }

        return diagnosticId == null ? null : diagnosticId.toString();
    }

    private StartSpanOptions createStartOption(SpanKind kind, OperationName operationName) {
        StartSpanOptions startOptions = new StartSpanOptions(kind)
            .setAttribute(MESSAGING_SYSTEM_ATTRIBUTE_NAME, "servicebus")
            .setAttribute(ENTITY_PATH_KEY, entityPath)
            .setAttribute(HOST_NAME_KEY, fullyQualifiedName);

        if (operationName != null) {
            startOptions.setAttribute(MESSAGING_OPERATION_ATTRIBUTE_NAME, operationName.toString());
        }

        return startOptions;
    }

    private <T> Mono<T> traceMonoWithLink(String spanName, OperationName operationName, Mono<T> publisher, ServiceBusMessage message, Context messageContext) {
        if (isEnabled()) {
            return Mono.defer(() -> {
                StartSpanOptions startSpanOptions = createStartOption(SpanKind.CLIENT, operationName);
                if (message != null) {
                    reportMessageSpan(message);
                    startSpanOptions.addLink(createLink(message.getApplicationProperties(), null, messageContext));
                }

                Context span = tracer.start(spanName, startSpanOptions, Context.NONE);

                return publisher
                    .doOnEach(signal -> {
                        if (signal.isOnComplete() || signal.isOnError()) {
                            endSpan(signal.getThrowable(), span, null);
                        }
                    })
                    .doOnCancel(() -> cancelSpan(span));
            });
        }

        return publisher;
    }


    /**
     * Traces arbitrary mono. No special send or receive semantics is applied.
     */
    private  <T> Mono<T> traceMono(String spanName, Mono<T> publisher, Context parent) {
        if (isEnabled()) {
            return Mono.defer(() -> {
                Context span = tracer.start(spanName, createStartOption(SpanKind.CLIENT, null), parent);
                return publisher
                    .doOnEach(signal -> {
                        if (signal.isOnComplete() || signal.isOnError()) {
                            endSpan(signal.getThrowable(), span, null);
                        }
                    })
                    .doOnCancel(() -> cancelSpan(span));
            });
        }

        return publisher;
    }

    void cancelSpan(Context span) {
        tracer.end("cancelled", null, span);
    }

    public enum OperationName {
        PUBLISH("publish"),
        RECEIVE("receive"),
        SETTLE("settle"),
        PROCESS("process");

        private final String operationName;
        OperationName(String operationName) {
            this.operationName = operationName;
        }

        @Override
        public String toString() {
            return operationName;
        }
    }
}
