// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.MESSAGE_ENQUEUED_TIME;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.AZ_TRACING_NAMESPACE_VALUE;

/**
 * Tracing helper.
 */
public class ServiceBusTracer {
    public static final String START_TIME_KEY = "span-start-time";
    public static final String REACTOR_PARENT_TRACE_CONTEXT_KEY = "otel-context-key";
    private static final AutoCloseable NOOP_CLOSEABLE = () -> {
    };
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusTracer.class);
    protected static final String TRACEPARENT_KEY = "traceparent";

    protected static final boolean IS_TRACING_DISABLED = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TRACING_DISABLED, false);
    protected final Tracer tracer;
    protected final String fullyQualifiedName;
    protected final String entityPath;

    public ServiceBusTracer(Tracer tracer, String fullyQualifiedName, String entityPath) {
        this.tracer = IS_TRACING_DISABLED ? null : tracer;
        this.fullyQualifiedName = Objects.requireNonNull(fullyQualifiedName, "'fullyQualifiedName' cannot be null");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null");
    }

    /**
     * Gets default tracer from SPI.
     */
    public static Tracer getDefaultTracer() {
        Iterable<Tracer> tracers = ServiceLoader.load(Tracer.class);
        Iterator<Tracer> it = tracers.iterator();
        return it.hasNext() ? it.next() : null;
    }

    /**
     * Checks if tracing is enabled.
     */
    public boolean isEnabled() {
        return tracer != null;
    }

    /**
     * Makes span in provided context (if any) current. Caller is responsible to close the returned scope.
     */
    public AutoCloseable makeSpanCurrent(Context span) {
        return tracer == null ? NOOP_CLOSEABLE : tracer.makeSpanCurrent(span);
    }

    /**
     * Traces arbitrary mono. No special send or receive semantics is applied.
     */
    public <T> Mono<T> traceMono(String spanName, Mono<T> publisher) {
        if (tracer != null) {
            return publisher
                .doOnEach(this::endSpan)
                .contextWrite(ctx -> ctx.put(REACTOR_PARENT_TRACE_CONTEXT_KEY, tracer.start(spanName, setAttributes(Context.NONE), ProcessKind.SEND)));
        }

        return publisher;
    }

    /**
     * Traces arbitrary mono that operates with received message as input, e.g. renewLock. No special send or receive semantics is applied.
     */
    public <T> Mono<T> traceMonoWithLink(String spanName, Mono<T> publisher, ServiceBusReceivedMessage message, Context messageContext) {
        if (tracer != null) {
            return publisher
                .doOnEach(this::endSpan)
                .contextWrite(ctx -> ctx.put(REACTOR_PARENT_TRACE_CONTEXT_KEY, startSpanWithLink(spanName, message, messageContext, Context.NONE)));
        }

        return publisher;
    }

    /**
     * Traces arbitrary mono that operates with sent message as input, e.g. schedule. No special send or receive semantics is applied.
     */
    public <T> Mono<T> traceMonoWithLink(String spanName, Mono<T> publisher, ServiceBusMessage message, Context messageContext) {
        if (tracer != null) {
            return publisher
                .doOnEach(this::endSpan)
                .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY,
                    startSpanWithLink(spanName, message, messageContext, Context.NONE)));
        }

        return publisher;
    }

    /**
     * Traces arbitrary mono that operates with batch of sent message as input, e.g. schedule. No special send or receive semantics is applied.
     */
    public <T> Flux<T> traceFluxWithLinks(String spanName, Flux<T> publisher, List<ServiceBusMessage> batch, Function<ServiceBusMessage, Context> getContext) {
        if (tracer != null) {
            return publisher
                .doOnEach(this::endSpan)
                .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY, startSpanWithLinks(spanName, batch, getContext, Context.NONE)));
        }
        return publisher;
    }

    /**
     * Ends span and scope.
     */
    public void endSpan(Throwable throwable, Context span, AutoCloseable scope) {
        if (tracer != null) {
            String errorCondition = "success";
            if (throwable instanceof AmqpException) {
                AmqpException exception = (AmqpException) throwable;
                errorCondition = exception.getErrorCondition().getErrorCondition();
            }

            try {
                if (scope != null) {
                    scope.close();
                }
            } catch (Exception e) {
                LOGGER.warning("Can't close scope", e);
            } finally {
                tracer.end(errorCondition, throwable, span);
            }
        }
    }

    /**
     * Used in ServiceBusMessageBatch.tryAddMessage() to start tracing for to-be-sent out messages.
     */
    public void reportMessageSpan(ServiceBusMessage serviceBusMessage, Context messageContext) {
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

    /**
     * Instruments peek or receiveDeferred that return a single message. Creates a single span, does not report any metrics
     */
    public Mono<ServiceBusReceivedMessage> traceManagementReceive(String spanName, Mono<ServiceBusReceivedMessage> publisher,
        Function<ServiceBusReceivedMessage, Context> getMessageContext) {
        if (tracer != null) {
            AtomicLong startTime = new AtomicLong();
            AtomicReference<ServiceBusReceivedMessage> message = new AtomicReference<>();
            return publisher.doOnEach(signal -> {
                if (signal.hasValue()) {
                    message.set(signal.get());
                }

                if (signal.isOnComplete() || signal.isOnError()) {
                    ServiceBusReceivedMessage msg = message.get();
                    Context messageContext = msg == null ? null : getMessageContext.apply(msg);

                    Context span = startSpanWithLink(spanName, msg, messageContext, new Context(START_TIME_KEY, startTime.get()));
                    endSpan(null, span, null);
                }
            })
            .doOnSubscribe(s -> {
                startTime.set(Instant.now().toEpochMilli());
            });
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
        if (tracer != null) {
            return messages
                .doOnEach(signal -> {
                    Context builder = signal.getContextView().getOrDefault(REACTOR_PARENT_TRACE_CONTEXT_KEY, Context.NONE);
                    if (signal.hasValue()) {
                        ServiceBusReceivedMessage message = signal.get();
                        if (message != null) {
                            addLink(message.getApplicationProperties(), message.getEnqueuedTime(), builder, Context.NONE);
                        }
                    } else if (signal.isOnComplete() || signal.isOnError()) {
                        Context span = tracer.start(spanName, builder, ProcessKind.SEND);
                        endSpan(signal.getThrowable(), span, null);
                    }
                })
                .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY,
                    getBuilder(spanName, new Context(START_TIME_KEY, Instant.now().toEpochMilli()))));
        }
        return messages;
    }

    public Context startSpanWithLinks(String spanName, List<ServiceBusMessage> batch, Function<ServiceBusMessage, Context> getMessageContext, Context parent) {
        if (tracer != null) {
            Context spanBuilder = getBuilder(spanName, parent);
            for (ServiceBusMessage message : batch) {
                createMessageSpanAndAddLink(message, spanBuilder, getMessageContext.apply(message));
            }

            return tracer.start(spanName, spanBuilder, ProcessKind.SEND);
        }

        return parent;
    }

    Context startSpanWithLink(String spanName, ServiceBusReceivedMessage message, Context messageContext, Context parent) {
        if (tracer != null) {
            Context spanBuilder = getBuilder(spanName, parent);
            if (message != null) {
                addLink(message.getApplicationProperties(), message.getEnqueuedTime(), spanBuilder, messageContext);
            }

            // TODO: need to refactor tracing in core. Currently we use ProcessKind.SEND as
            // SpanKind.CLIENT
            return tracer.start(spanName, spanBuilder, ProcessKind.SEND);
        }

        return parent;
    }

    /**
     * Starts span. Used by ServiceBus*Instrumentations.
     */
    Context startProcessSpan(String spanName, ServiceBusReceivedMessage message, Context parent) {
        if (tracer != null) {
            return tracer.start(spanName, setParentAndAttributes(message, parent), ProcessKind.PROCESS);
        }

        return parent;
    }

    private Context startSpanWithLink(String name, ServiceBusMessage message, Context messageContext, Context parent) {
        if (tracer != null) {
            Context spanBuilder = getBuilder(name, parent);
            if (message != null) {
                createMessageSpanAndAddLink(message, spanBuilder, messageContext);
            }

            return tracer.start(name, spanBuilder, ProcessKind.SEND);
        }

        return parent;
    }

    private void createMessageSpanAndAddLink(ServiceBusMessage message, Context spanBuilder, Context messageContext) {
        if (tracer != null) {
            String traceparent = getTraceparent(message.getApplicationProperties());

            if (traceparent == null) {
                reportMessageSpan(message, messageContext);
            }

            addLink(message.getApplicationProperties(), null, spanBuilder, messageContext);
        }
    }

    private void addLink(Map<String, Object> applicationProperties, OffsetDateTime enqueuedTime, Context spanBuilder, Context messageContext) {
        if (tracer != null) {
            Optional<Object> linkContext = messageContext == null ? Optional.empty() : messageContext.getData(SPAN_CONTEXT_KEY);
            if (!linkContext.isPresent()) {
                String traceparent = getTraceparent(applicationProperties);
                Context link = traceparent == null ? Context.NONE : tracer.extractContext(traceparent, Context.NONE);
                linkContext = link.getData(SPAN_CONTEXT_KEY);
            }

            if (enqueuedTime != null) {
                spanBuilder = spanBuilder.addData(MESSAGE_ENQUEUED_TIME, enqueuedTime.toInstant().atOffset(ZoneOffset.UTC).toEpochSecond());
            }

            if (linkContext.isPresent()) {
                tracer.addLink(spanBuilder.addData(SPAN_CONTEXT_KEY, linkContext.get()));
            }
        }
    }

    private Context setParentAndAttributes(ServiceBusReceivedMessage message, Context parent) {
        if (message.getEnqueuedTime() != null) {
            parent = parent.addData(MESSAGE_ENQUEUED_TIME, message.getEnqueuedTime().toInstant().atOffset(ZoneOffset.UTC).toEpochSecond());
        }

        parent = getParent(message.getApplicationProperties(), parent);

        return parent
            .addData(Tracer.ENTITY_PATH_KEY, entityPath)
            .addData(HOST_NAME_KEY, fullyQualifiedName)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
    }

    private Context getParent(Map<String, Object> properties, Context context) {
        if (tracer == null) {
            return context;
        }

        String traceparent = getTraceparent(properties);
        return traceparent == null ? context : tracer.extractContext(traceparent, context);
    }

    private static String getTraceparent(Map<String, Object> applicationProperties) {
        Object diagnosticId = applicationProperties.get(DIAGNOSTIC_ID_KEY);
        if (diagnosticId == null) {
            diagnosticId = applicationProperties.get(TRACEPARENT_KEY);
        }

        return diagnosticId == null ? null : diagnosticId.toString();
    }

    private Context setAttributes(Context context) {
        return context
            .addData(ENTITY_PATH_KEY, entityPath)
            .addData(HOST_NAME_KEY, fullyQualifiedName)
            .addData(AZ_TRACING_NAMESPACE_KEY, AZ_TRACING_NAMESPACE_VALUE);
    }

    private Context getBuilder(String spanName, Context context) {
        if (tracer != null) {
            return setAttributes(tracer.getSharedSpanBuilder(spanName, context));
        }

        return context;
    }

    private <T> void endSpan(Signal<T> signal) {
        if (tracer == null) {
            return;
        }

        Context span = signal.getContextView().getOrDefault(REACTOR_PARENT_TRACE_CONTEXT_KEY, Context.NONE);
        endSpan(signal.getThrowable(), span, null);
    }
}
