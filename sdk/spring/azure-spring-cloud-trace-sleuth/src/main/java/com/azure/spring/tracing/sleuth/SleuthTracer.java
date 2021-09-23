// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.tracing.sleuth;

import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.spring.tracing.sleuth.implementation.AmqpPropagationFormatUtil;
import com.azure.spring.tracing.sleuth.implementation.AmqpTraceUtil;
import com.azure.spring.tracing.sleuth.implementation.HttpTraceUtil;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Basic tracing implementation class for use with REST and AMQP Service Clients to create {@link Span} and in-process
 * context propagation. Accepted Sleuth tracer capable of starting and exporting spans.
 * <p>
 * This helper class will not support W3C distributed tracing protocol and injects SpanContext into the outgoing HTTP
 * and AMQP requests.
 */
public class SleuthTracer implements com.azure.core.util.tracing.Tracer {

    private final Tracer tracer;
    private final CurrentTraceContext currentTraceContext;
    private final Propagator propagator;

    public SleuthTracer(Tracer tracer, CurrentTraceContext currentTraceContext, Propagator propagator) {
        Assert.notNull(tracer, "tracer must not be null!");
        Assert.notNull(currentTraceContext, "currentTraceContext must not be null!");
        Assert.notNull(propagator, "propagator must not be null!");
        this.tracer = tracer;
        this.currentTraceContext = currentTraceContext;
        this.propagator = propagator;
    }

    static final String AZ_NAMESPACE_KEY = "az.namespace";

    // standard attributes with AMQP request
    static final String MESSAGE_BUS_DESTINATION = "message_bus.destination";
    static final String PEER_ENDPOINT = "peer.address";

    private final ClientLogger logger = new ClientLogger(SleuthTracer.class);
    private static final AutoCloseable NOOP_CLOSEABLE = () -> {
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, Context context) {
        Objects.requireNonNull(spanName, "'spanName' cannot be null.");
        Span.Builder spanBuilder = createSpanBuilder(spanName, null, SpanKind.INTERNAL, null, context);

        return startSpanInternal(spanBuilder, null, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, StartSpanOptions options, Context context) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        Span.Builder spanBuilder = createSpanBuilder(spanName, null, options.getSpanKind(),
            options.getAttributes(), context);

        return startSpanInternal(spanBuilder, null, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, Context context, ProcessKind processKind) {
        Objects.requireNonNull(spanName, "'spanName' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");
        Objects.requireNonNull(processKind, "'processKind' cannot be null.");

        Span.Builder spanBuilder;
        switch (processKind) {
            case SEND:
                // use previously created span builder from the LINK process.
                spanBuilder = getOrDefault(context, SPAN_BUILDER_KEY, null, Span.Builder.class);
                if (spanBuilder == null) {
                    return context;
                }
                return startSpanInternal(spanBuilder, this::addMessagingTags, context);
            case MESSAGE:
                spanBuilder = createSpanBuilder(spanName, null, SpanKind.PRODUCER, null, context);
                context = startSpanInternal(spanBuilder, this::addMessagingTags, context);
                return setDiagnosticId(context);
            case PROCESS:
                TraceContext remoteParentContext = getOrDefault(context, SPAN_CONTEXT_KEY, null, TraceContext.class);
                spanBuilder = createSpanBuilder(spanName, remoteParentContext, SpanKind.CONSUMER, null, context);
                context = startSpanInternal(spanBuilder, this::addMessagingTags, context);

                // TODO(moary): Refer to opentelemetry implementation. We should do this in the EventHub/ServiceBus SDK
                //  instead to make sure scope is
                //  closed in the same thread where it was started to prevent leaking the context.
                return context.addData(SCOPE_KEY, makeSpanCurrent(context));
            default:
                return context;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(int responseCode, Throwable throwable, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null.");
        Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span == null) {
            return;
        }

        if (!span.isNoop()) {
            span = HttpTraceUtil.setSpanStatus(span, responseCode, throwable);
        }
        span.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String key, String value, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null");
        if (CoreUtils.isNullOrEmpty(value)) {
            logger.verbose("Failed to set span attribute since value is null or empty.");
            return;
        }

        final Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span != null) {
            span.tag(key, value);
        } else {
            logger.verbose("Failed to find span to add tag.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context setSpanName(String spanName, Context context) {
        return context.addData(USER_SPAN_NAME_KEY, spanName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(String statusMessage, Throwable throwable, Context context) {
        Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span == null) {
            logger.verbose("Failed to find span to end it.");
            return;
        }

        if (!span.isNoop()) {
            span = AmqpTraceUtil.parseStatusMessage(span, statusMessage, throwable);
        }

        span.end();

        // TODO (limolkova): Refer to opentelemetry implementation. Remove once ServiceBus/EventHub start making
        //  span current explicitly.
        endScope(context);
    }

    @Override
    public void addLink(Context context) {
        logger.warning("Spring Sleuth does not support the link feature.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context extractContext(String diagnosticId, Context context) {
        return AmqpPropagationFormatUtil.extractContext(diagnosticId, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context getSharedSpanBuilder(String spanName, Context context) {
        // this is used to create messaging send spanBuilder, and it's a CLIENT span
        return context.addData(SPAN_BUILDER_KEY, createSpanBuilder(spanName, null, SpanKind.CLIENT, null, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AutoCloseable makeSpanCurrent(Context context) {
        Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span == null) {
            return NOOP_CLOSEABLE;
        }

        return currentTraceContext.newScope(span.context());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation")
    public void addEvent(String eventName, Map<String, Object> traceEventAttributes, OffsetDateTime timestamp) {
        addEvent(eventName, traceEventAttributes, timestamp, new Context(PARENT_SPAN_KEY, tracer.currentSpan()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEvent(String eventName, Map<String, Object> traceEventAttributes, OffsetDateTime timestamp,
                         Context context) {
        Objects.requireNonNull(eventName, "'eventName' cannot be null.");

        Span currentSpan = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (currentSpan == null) {
            logger.verbose("Failed to find a starting span to associate the {} with.", eventName);
            return;
        }

        if (timestamp == null) {
            addEventAndTags(currentSpan,
                eventName,
                traceEventAttributes == null ? Collections.emptyMap() : traceEventAttributes,
                null);
        } else {
            addEventAndTags(currentSpan,
                eventName,
                traceEventAttributes == null ? Collections.emptyMap() : traceEventAttributes,
                timestamp.toInstant()
            );
        }
    }

    private void addEventAndTags(Span currentSpan,
                                 String eventName,
                                 Map<String, Object> traceEventAttributes,
                                 Instant timestamp) {
        currentSpan.event(eventName);
        Map<String, String> attributes = convertToSleuthTags(traceEventAttributes);
        attributes.entrySet().forEach(
            (entry) -> currentSpan.tag(entry.getKey(), entry.getValue()));
        if (timestamp != null) {
            currentSpan.tag("event_timestamp", timestamp.toString());
        }
    }

    /**
     * Returns a {@link Span.Builder} to create and start a new child {@link Span} with parent being the designated
     * {@link Span}.
     *
     * @param spanBuilder Span.Builder for the span. Must be created before calling this method
     * @param setAttributes Callback to populate attributes for the span.
     * @return A {@link Context} with created {@link Span}.
     */
    private Context startSpanInternal(Span.Builder spanBuilder,
                                      java.util.function.BiConsumer<Span, Context> setAttributes,
                                      Context context) {
        Objects.requireNonNull(spanBuilder, "'spanBuilder' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        Span span = spanBuilder.start();
        // BraveTraceContext.toBrave(span.context()).sampled()
        if (!span.isNoop()) {
            // If span is sampled in, add additional attributes

            String tracingNamespace = getOrDefault(context, AZ_TRACING_NAMESPACE_KEY, null, String.class);
            if (tracingNamespace != null) {
                span.tag(AZ_NAMESPACE_KEY, tracingNamespace);
            }

            if (setAttributes != null) {
                setAttributes.accept(span, context);
            }
        }
        return context.addData(PARENT_SPAN_KEY, span);
    }

    /**
     * Returns a {@link Span.Builder} to create and start a new child {@link Span} with parent being the designated
     * {@code Span}.
     *
     * @param spanName The name of the returned Span.
     * @param remoteParentContext Remote parent context if any, or {@code null} otherwise.
     * @param spanKind Kind of the span to create.
     * @param beforeSaplingAttributes Optional attributes available when span starts and important for sampling.
     * @param context The context containing the span and the span name.
     * @return A {@code Span.SpanBuilder} to create and start a new {@code Span}.
     */
    @SuppressWarnings("unchecked")
    private Span.Builder createSpanBuilder(String spanName,
                                           TraceContext remoteParentContext,
                                           SpanKind spanKind,
                                           Map<String, Object> beforeSaplingAttributes,
                                           Context context) {

        String spanNameKey = getOrDefault(context, USER_SPAN_NAME_KEY, null, String.class);

        if (spanNameKey == null) {
            spanNameKey = spanName;
        }

        Span.Builder spanBuilder = tracer.spanBuilder()
                                         .name(spanNameKey)
                                         .kind(convertToSleuthKind(spanKind));

        // if remote parent is provided, it has higher priority
        if (remoteParentContext != null) {
            spanBuilder.setParent(remoteParentContext);
        } else {
            Span parentSpan = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
            if (parentSpan == null) {
                parentSpan = tracer.currentSpan();
            }
            if (parentSpan != null) {
                spanBuilder.setParent(parentSpan.context());
            } else {
                spanBuilder.setNoParent();
            }
        }

        // if some attributes are provided, set them
        if (!CoreUtils.isNullOrEmpty(beforeSaplingAttributes)) {
            Map<String, String> attributes = convertToSleuthTags(beforeSaplingAttributes);
            attributes.entrySet().forEach(
                (entry) -> spanBuilder.tag(entry.getKey(), entry.getValue()));
        }

        return spanBuilder;
    }

    /**
     * Ends current scope on the context.
     *
     * @param context Context instance with the scope to end.
     */
    private void endScope(Context context) {
        CurrentTraceContext.Scope scope = getOrDefault(context, SCOPE_KEY, null, CurrentTraceContext.Scope.class);
        if (scope != null) {
            scope.close();
        }
    }

    /*
     * Converts our SpanKind to Sleuth SpanKind.
     * Map the SpanKind.INTERNAL SpanKind.CLIENT to Span.Kind.CLIENT.
     */
    private Span.Kind convertToSleuthKind(com.azure.core.util.tracing.SpanKind kind) {
        switch (kind) {
            case SERVER:
                return Span.Kind.SERVER;

            case CONSUMER:
                return Span.Kind.CONSUMER;

            case PRODUCER:
                return Span.Kind.PRODUCER;

            default:
                return Span.Kind.CLIENT;
        }
    }

    /**
     * Maps span/event properties to Sleuth tags tagging.
     *
     * @param tags the tags provided by the client SDK's.
     * @return Key value for Brave tagging.
     */
    private Map<String, String> convertToSleuthTags(Map<String, Object> tags) {
        Map<String, String> attrs = new HashMap<>();
        tags.forEach((key, value) -> {
            if (value instanceof Boolean
                || value instanceof String
                || value instanceof Double
                || value instanceof Long
                || value instanceof String[]
                || value instanceof long[]
                || value instanceof double[]
                || value instanceof boolean[]) {
                attrs.put(key, StringUtils.arrayToCommaDelimitedString((Object[]) value));
            } else {
                logger.warning("Could not populate tags with key '{}', type is not supported.", key);
            }
        });
        return attrs;
    }

    /**
     * Extracts the {@link TraceContext trace identifiers} and the {@link TraceContext} of the current tracing span as
     * text and returns in a {@link Context} object.
     *
     * @param context The context with current tracing span describing unique message context.
     * @return The {@link Context} containing the {@link TraceContext} and trace-parent of the current span.
     */
    private Context setDiagnosticId(Context context) {
        Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span == null) {
            return context;
        }

        TraceContext traceContext = span.context();
        final String traceparent = AmqpPropagationFormatUtil.getDiagnosticId(traceContext);
        if (traceparent == null) {
            return context;
        }
        return context.addData(DIAGNOSTIC_ID_KEY, traceparent).addData(SPAN_CONTEXT_KEY, traceContext);
    }

    /**
     * Extracts request attributes from the given {@code context} and adds it to the started span.
     *
     * @param span The span to which request attributes are to be added.
     * @param context The context containing the request attributes.
     */
    private void addMessagingTags(Span span, Context context) {
        Objects.requireNonNull(span, "'span' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        String entityPath = getOrDefault(context, ENTITY_PATH_KEY, null, String.class);
        if (entityPath != null) {
            span.tag(MESSAGE_BUS_DESTINATION, entityPath);
        }
        String hostName = getOrDefault(context, HOST_NAME_KEY, null, String.class);
        if (hostName != null) {
            span.tag(PEER_ENDPOINT, hostName);
        }
        Long messageEnqueuedTime = getOrDefault(context, MESSAGE_ENQUEUED_TIME, null, Long.class);
        if (messageEnqueuedTime != null) {
            span.tag(MESSAGE_ENQUEUED_TIME, String.valueOf(messageEnqueuedTime));
        }
    }

    /**
     * Returns the value of the specified key from the context.
     *
     * @param key The name of the attribute that needs to be extracted from the {@code Context}.
     * @param defaultValue the value to return in data not found.
     * @param clazz clazz the type of raw class to find data for.
     * @param context The context containing the specified key.
     * @return The T type of raw class object
     */
    @SuppressWarnings("unchecked")
    private <T> T getOrDefault(Context context, String key, T defaultValue, Class<T> clazz) {
        final Optional<Object> optional = context.getData(key);
        final Object result = optional.filter(value -> clazz.isAssignableFrom(value.getClass())).orElseGet(() -> {
            logger.verbose("Could not extract key '{}' of type '{}' from context.", key, clazz);
            return defaultValue;
        });

        return (T) result;
    }
}
