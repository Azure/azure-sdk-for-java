// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.tracing;

import io.clientcore.core.util.Context;
import com.azure.core.v2.util.TracingOptions;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.azure.core.v2.util.tracing.Utils.SPAN_KIND_KEY;
import static com.azure.core.v2.util.tracing.Utils.START_TIME_KEY;
import static com.azure.core.v2.util.tracing.Utils.addMessagingAttributes;
import static com.azure.core.v2.util.tracing.Utils.getOrNull;

/**
 * Contract that all tracers must implement to be pluggable into the SDK.
 *
 * @see TracerProxy
 */
public interface Tracer {
    /**
     * Key for {@link Context} which indicates that the context contains parent span data. This span will be used
     * as the parent span for all spans the SDK creates.
     * <p>
     * If no span data is listed when the span is created it will default to using this span key as the parent span.
     *
     * @deprecated Deprecated in favor of PARENT_TRACE_CONTEXT_KEY, use it to propagate full io.opentelemetry.Context
     */
    @Deprecated
    String PARENT_SPAN_KEY = "parent-span";

    /**
     * {@link Context} key to store trace context. This context will be used as a parent context
     * for new spans and propagated in outgoing HTTP calls.
     *
     */
    String PARENT_TRACE_CONTEXT_KEY = "trace-context";

    /**
     * Key for {@link Context} which indicates that the context contains the name for the user spans that are
     * created.
     * <p>
     * If no span name is listed when the span is created it will default to using the calling method's name.
     *
     * @deprecated please pass span name to Tracer.start methods.
     */
    @Deprecated
    String USER_SPAN_NAME_KEY = "user-span-name";

    /**
     * Key for {@link Context} which indicates that the context contains an entity path.
     */
    String ENTITY_PATH_KEY = "entity-path";

    /**
     * Key for {@link Context} which indicates that the context contains the hostname.
     */
    String HOST_NAME_KEY = "hostname";

    /**
     * Key for {@link Context} which indicates that the context contains a message span context.
     */
    String SPAN_CONTEXT_KEY = "span-context";

    /**
     * Key for {@link Context} which indicates that the context contains a "Diagnostic Id" for the service call.
     *
     * @deprecated use {@link Tracer#extractContext(Function)} and {@link Tracer#injectContext(BiConsumer, Context)}
     *             for context propagation.
     */
    @Deprecated
    String DIAGNOSTIC_ID_KEY = "Diagnostic-Id";

    /**
     * Key for {@link Context} the scope of code where the given Span is in the current Context.
     *
     * @deprecated use {@link Tracer#makeSpanCurrent(Context)} instead.
     */
    @Deprecated
    String SCOPE_KEY = "scope";

    /**
     * Key for {@link Context} which indicates that the context contains the Azure resource provider namespace.
     *
     * @deprecated Pass Azure Resource Provider Namespace to Tracer factory method {@link TracerProvider#createTracer(String, String, String, TracingOptions)}
     */
    @Deprecated
    String AZ_TRACING_NAMESPACE_KEY = "az.namespace";

    /**
     * Key for {@link Context} which indicates the shared span builder that is in the current Context.
     *
     * @deprecated use {@link StartSpanOptions#addLink(TracingLink)} instead
     */
    @Deprecated
    String SPAN_BUILDER_KEY = "builder";

    /**
     * Key for {@link Context} which indicates the time of the last enqueued message in the partition's stream.
     *
     * @deprecated Use {@link StartSpanOptions#addLink(TracingLink)} and pass enqueued time as an attribute on link.
     */
    @Deprecated
    String MESSAGE_ENQUEUED_TIME = "x-opt-enqueued-time";

    /**
     * Key for {@link Context} which disables tracing for the request associated with the current context.
     */
    String DISABLE_TRACING_KEY = "disable-tracing";

    /**
     * Creates a new tracing span.
     * <p>
     * The {@code context} will be checked for information about a parent span. If a parent span is found, the new span
     * will be added as a child. Otherwise, the parent span will be created and added to the {@code context} and any
     * downstream {@code start()} calls will use the created span as the parent.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Starts a tracing span with provided method name and explicit parent span</p>
     * <!-- src_embed com.azure.core.util.tracing.start#name -->
     * <!-- end com.azure.core.util.tracing.start#name -->
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the returned span.
     * @throws NullPointerException if {@code methodName} or {@code context} is {@code null}.
     */
    Context start(String methodName, Context context);

    /**
     * Creates a new tracing span.
     * <p>
     * The {@code context} will be checked for information about a parent span. If a parent span is found, the new span
     * will be added as a child. Otherwise, the parent span will be created and added to the {@code context} and any
     * downstream {@code start()} calls will use the created span as the parent.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Starts a tracing span with provided method name and explicit parent span</p>
     * <!-- src_embed com.azure.core.util.tracing.start#options -->
     * <!-- end com.azure.core.util.tracing.start#options -->
     *
     * @param methodName Name of the method triggering the span creation.
     * @param options span creation options.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the returned span.
     * @throws NullPointerException if {@code options} or {@code context} is {@code null}.
     */
    default Context start(String methodName, StartSpanOptions options, Context context) {
        // fall back to old API if not overridden.
        return start(methodName, context);
    }

    /**
     * Creates a new tracing span for AMQP calls.
     *
     * <p>
     * The {@code context} will be checked for information about a parent span. If a parent span is found, the new span
     * will be added as a child. Otherwise, the parent span will be created and added to the {@code context} and any
     * downstream {@code start()} calls will use the created span as the parent.
     *
     * <p>
     * Sets additional request attributes on the created span when {@code processKind} is
     * {@link ProcessKind#SEND ProcessKind.SEND}.
     *
     * <p>
     * Returns the diagnostic Id and span context of the returned span when {@code processKind} is
     * {@link ProcessKind#MESSAGE ProcessKind.MESSAGE}.
     *
     * <p>
     * Creates a new tracing span with remote parent and returns that scope when the given when {@code processKind}
     * is {@link ProcessKind#PROCESS ProcessKind.PROCESS}.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Starts a tracing span with provided method name and AMQP operation SEND</p>
     *
     * @param spanName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @param processKind AMQP operation kind.
     * @return The updated {@link Context} object containing the returned span.
     * @throws NullPointerException if {@code methodName} or {@code context} or {@code processKind} is {@code null}.
     *
     * @deprecated use {@link Tracer#start(String, StartSpanOptions, Context)} instead.
     */
    @Deprecated
    default Context start(String spanName, Context context, ProcessKind processKind) {
        Objects.requireNonNull(spanName, "'spanName' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");
        Objects.requireNonNull(processKind, "'processKind' cannot be null.");

        if (!isEnabled()) {
            return context;
        }

        StartSpanOptions spanBuilder;
        switch (processKind) {
            case SEND:
                // use previously created span builder with the links
                spanBuilder = getOrNull(context, SPAN_BUILDER_KEY, StartSpanOptions.class);
                if (spanBuilder == null) {
                    // we can't return context here, because caller would not know that span was not created.
                    // it will add attributes or events to parent span and end parent span.
                    Utils.LOGGER.atWarning()
                        .addKeyValue("spanName", spanName)
                        .addKeyValue("processKind", processKind)
                        .log("Start span is called without builder on the context, creating default builder.");
                    spanBuilder = new StartSpanOptions(SpanKind.CLIENT);
                }

                addMessagingAttributes(spanBuilder, context);
                return start(spanName, spanBuilder, context);

            case MESSAGE:
                spanBuilder = new StartSpanOptions(SpanKind.PRODUCER);
                addMessagingAttributes(spanBuilder, context);
                context = start(spanName, spanBuilder, context);

                AtomicReference<String> diagnosticId = new AtomicReference<>();
                injectContext((name, value) -> {
                    if (name.equals("traceparent")) {
                        diagnosticId.set(value);
                    }
                }, context);
                return context.put(DIAGNOSTIC_ID_KEY, diagnosticId);

            case PROCESS:
                // use previously created span builder with the links
                spanBuilder = getOrNull(context, SPAN_BUILDER_KEY, StartSpanOptions.class);
                if (spanBuilder == null) {
                    // if there is no builder, create new one from parent in context
                    spanBuilder = new StartSpanOptions(SpanKind.CONSUMER).setRemoteParent(context);
                }
                addMessagingAttributes(spanBuilder, context);
                return start(spanName, spanBuilder, context);

            default:
                Utils.LOGGER.atWarning()
                    .addKeyValue("spanName", spanName)
                    .addKeyValue("processKind", processKind)
                    .log("Start span is called with unknown process kind, suppressing the span.");
                return context;
        }
    }

    /**
     * Completes the current tracing span.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Completes the tracing span present in the context, with the corresponding OpenTelemetry status for the given
     * response status code</p>
     *
     * @param responseCode Response status code if the span is in an HTTP call context.
     * @param error {@link Throwable} that happened during the span or {@code null} if no exception occurred.
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code context} is {@code null}.
     *
     * @deprecated set specific attribute e.g. http_status_code explicitly and use {@link Tracer#end(String, Throwable, Context)}.
     */
    @Deprecated
    default void end(int responseCode, Throwable error, Context context) {
        end(null, error, context);
    }

    /**
     * Completes span on the context.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Completes the tracing span with unset status</p>
     *
     * <!-- src_embed com.azure.core.util.tracing.end#success -->
     * <!-- end com.azure.core.util.tracing.end#success -->
     *
     * <p>Completes the tracing span with provided error message</p>
     *
     * <!-- src_embed com.azure.core.util.tracing.end#errorStatus -->
     * <!-- end com.azure.core.util.tracing.end#errorStatus -->
     *
     * <p>Completes the tracing span with provided exception</p>
     *
     * <!-- src_embed com.azure.core.util.tracing.end#exception -->
     * <!-- end com.azure.core.util.tracing.end#exception -->
     *
     * @param errorMessage The error message that occurred during the call, or {@code null} if no error.
     *   occurred. Any other non-null string indicates an error with description provided in {@code errorMessage}.
     *
     * @param throwable {@link Throwable} that happened during the span or {@code null} if no exception occurred.
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code context} is {@code null}.
     */
    void end(String errorMessage, Throwable throwable, Context context);

    /**
     * Adds metadata to the current span. If no span information is found in the context, then no metadata is added.
     * <!-- src_embed com.azure.core.util.tracing.set-attribute#string -->
     * <!-- end com.azure.core.util.tracing.set-attribute#string -->
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code key} or {@code value} or {@code context} is {@code null}.
     */
    void setAttribute(String key, String value, Context context);

    /**
     * Sets long attribute.
     *
     * <!-- src_embed com.azure.core.util.tracing.set-attribute#int -->
     * <!-- end com.azure.core.util.tracing.set-attribute#int -->
     * @param key attribute name
     * @param value atteribute value
     * @param context tracing context
     */
    default void setAttribute(String key, long value, Context context) {
        setAttribute(key, Long.toString(value), context);
    }

    /**
     * Sets an attribute on span.
     * Adding duplicate attributes, update, or removal is discouraged, since underlying implementations
     * behavior can vary.
     *
     * @param key attribute key.
     * @param value attribute value. Note that underlying tracer implementations limit supported value types.
     *              OpenTelemetry implementation supports following types:
     * <ul>
     *     <li>{@link String}</li>
     *     <li>{@code int}</li>
     *     <li>{@code double}</li>
     *     <li>{@code boolean}</li>
     *     <li>{@code long}</li>
     * </ul>
     * @param context context containing span to which attribute is added.
     */
    default void setAttribute(String key, Object value, Context context) {
        Objects.requireNonNull(value, "'value' cannot be null.");
        setAttribute(key, value.toString(), context);
    }

    /**
     * Sets the name for spans that are created.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Retrieve the span name of the returned span</p>
     *
     * @param spanName Name to give the next span.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the name of the returned span.
     * @throws NullPointerException if {@code spanName} or {@code context} is {@code null}.
     * @deprecated not needed.
     */
    @Deprecated
    default Context setSpanName(String spanName, Context context) {
        return context;
    }

    /**
     * Provides a way to link multiple tracing spans.
     * Used in batching operations to relate multiple requests under a single batch.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Link multiple spans using their span context information</p>
     *
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code context} is {@code null}.
     *
     * @deprecated use {@link StartSpanOptions#addLink(TracingLink)} )}
     */
    @Deprecated
    default void addLink(Context context) {
        if (!isEnabled()) {
            return;
        }

        final StartSpanOptions spanBuilder = getOrNull(context, SPAN_BUILDER_KEY, StartSpanOptions.class);
        if (spanBuilder == null) {
            return;
        }

        TracingLink link;
        Long messageEnqueuedTime = getOrNull(context, MESSAGE_ENQUEUED_TIME, Long.class);
        if (messageEnqueuedTime != null) {
            Map<String, Object> linkAttributes = new HashMap<>(1);
            linkAttributes.put(MESSAGE_ENQUEUED_TIME, messageEnqueuedTime);
            link = new TracingLink(context, linkAttributes);
        } else {
            link = new TracingLink(context);
        }

        spanBuilder.addLink(link);
    }

    /**
     * Extracts the span's context as {@link Context} from upstream.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Extracts the corresponding span context information from a valid diagnostic id</p>
     *
     * @param diagnosticId Unique identifier for the trace information of the span.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the span context.
     * @throws NullPointerException if {@code diagnosticId} or {@code context} is {@code null}.
     * @deprecated use {@link Tracer#extractContext(Function)}
     */
    @Deprecated
    default Context extractContext(String diagnosticId, Context context) {
        return extractContext((name) -> {
            if (name.equals("traceparent") || name.equals(DIAGNOSTIC_ID_KEY)) {
                return diagnosticId;
            }
            return null;
        });
    }

    /**
     * Extracts the span's context as {@link Context} from upstream.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Extracts the corresponding span context information from a valid diagnostic id</p>
     * <!-- src_embed com.azure.core.util.tracing.start#remote-parent-extract -->
     * <!-- end com.azure.core.util.tracing.start#remote-parent-extract -->
     *
     * @param headerGetter Unique identifier for the trace information of the span and todo.
     * @return The updated {@link Context} object containing the span context.
     * @throws NullPointerException if {@code diagnosticId} or {@code context} is {@code null}.
     */
    default Context extractContext(Function<String, String> headerGetter) {
        return Context.none();
    }

    /**
     * Injects tracing context.
     *
     * <!-- src_embed com.azure.core.util.tracing.injectContext -->
     * <!-- end com.azure.core.util.tracing.injectContext -->
     * @param headerSetter callback to set context with.
     * @param context trace context instance
     */
    default void injectContext(BiConsumer<String, String> headerSetter, Context context) {
    }

    /**
     * Returns a span builder with the provided name in {@link Context}.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Returns a builder with the provided span name.</p>
     *
     * @param spanName Name to give the span for the created builder.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the span builder.
     * @throws NullPointerException if {@code context} or {@code spanName} is {@code null}.
     * @deprecated use {@link StartSpanOptions#addLink(TracingLink)} instead
     */
    @Deprecated
    default Context getSharedSpanBuilder(String spanName, Context context) {
        if (!isEnabled()) {
            return context;
        }

        com.azure.core.v2.util.tracing.SpanKind spanKind
            = getOrNull(context, SPAN_KIND_KEY, com.azure.core.v2.util.tracing.SpanKind.class);
        if (spanKind == null) {
            spanKind = com.azure.core.v2.util.tracing.SpanKind.CLIENT;
        }

        StartSpanOptions options = new StartSpanOptions(spanKind);

        Instant startTime = getOrNull(context, START_TIME_KEY, Instant.class);
        if (startTime != null) {
            options.setStartTimestamp(startTime);
        }

        return context.put(SPAN_BUILDER_KEY, options);
    }

    /**
     * Adds an event to the current span with the provided {@code timestamp} and {@code attributes}.
     * <p>This API does not provide any normalization if provided timestamps are out of range of the current
     * span timeline</p>
     * <p>Supported attribute values include String, double, boolean, long, String [], double [], long [].
     * Any other Object value type and null values will be silently ignored.</p>
     *
     * @param name the name of the event.
     * @param attributes the additional attributes to be set for the event.
     * @param timestamp The instant, in UTC, at which the event will be associated to the span.
     * @throws NullPointerException if {@code eventName} is {@code null}.
     * @deprecated Use {@link #addEvent(String, Map, OffsetDateTime, Context)}
     */
    @Deprecated
    default void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp) {
        addEvent(name, attributes, timestamp, Context.none());
    }

    /**
     * Adds an event to the span present in the {@code Context} with the provided {@code timestamp}
     * and {@code attributes}.
     * <p>This API does not provide any normalization if provided timestamps are out of range of the current
     * span timeline</p>
     * <p>Supported attribute values include String, double, boolean, long, String [], double [], long [].
     * Any other Object value type and null values will be silently ignored.</p>
     *
     * <!-- src_embed com.azure.core.util.tracing.addEvent -->
     * <!-- end com.azure.core.util.tracing.addEvent -->
     *
     * @param name the name of the event.
     * @param attributes the additional attributes to be set for the event.
     * @param timestamp The instant, in UTC, at which the event will be associated to the span.
     * @param context the call metadata containing information of the span to which the event should be associated with.
     * @throws NullPointerException if {@code eventName} is {@code null}.
     */
    default void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp, Context context) {

    }

    /**
     * Makes span current. Implementations may put it on ThreadLocal.
     * Make sure to always use try-with-resource statement with makeSpanCurrent
     * @param context Context with span.
     *
     * <!-- src_embed com.azure.core.util.tracing.makeCurrent -->
     * <!-- end com.azure.core.util.tracing.makeCurrent -->
     *
     * @return Closeable that should be closed in the same thread with try-with-resource statement.
     */
    default AutoCloseable makeSpanCurrent(Context context) {
        return NoopTracer.INSTANCE.makeSpanCurrent(context);
    }

    /**
     * Checks if span is sampled in.
     *
     * @param span Span to check.
     * @return true if span is recording, false otherwise.
     */
    default boolean isRecording(Context span) {
        return true;
    }

    /**
     * Checks if tracer is enabled.
     *
     * <!-- src_embed com.azure.core.util.tracing.isEnabled -->
     * <!-- end com.azure.core.util.tracing.isEnabled -->
     *
     * @return true if tracer is enabled, false otherwise.
     */
    default boolean isEnabled() {
        return true;
    }
}
