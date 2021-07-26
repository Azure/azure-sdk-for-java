// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import com.azure.core.util.Context;

import java.time.OffsetDateTime;
import java.util.Map;

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
     * If no span data is listed when the SDK creates its first span, this span key will be used as the parent span.
     */
    String PARENT_SPAN_KEY = "parent-span";

    /**
     * Key for {@link Context} which indicates that the context contains the name for the user spans that are
     * created.
     * <p>
     * If no span name is listed when the span is created it will default to using the calling method's name.
     */
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
     */
    String DIAGNOSTIC_ID_KEY = "Diagnostic-Id";

    /**
     * Key for {@link Context} the scope of code where the given Span is in the current Context.
     */
    String SCOPE_KEY = "scope";

    /**
     * Key for {@link Context} which indicates that the context contains the Azure resource provider namespace.
     */
    String AZ_TRACING_NAMESPACE_KEY = "az.namespace";

    /**
     * Key for {@link Context} which indicates the shared span builder that is in the current Context.
     */
    String SPAN_BUILDER_KEY = "builder";

    /**
     * Key for {@link Context} which indicates the the time of the last enqueued message in the partition's stream.
     */
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
     * {@codesnippet com.azure.core.util.tracing.start#string-context}
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
     * {@codesnippet com.azure.core.util.tracing.start#options-context}
     *
     * @param methodName Name of the method triggering the span creation.
     * @param options span creation options.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the returned span.
     * @throws NullPointerException if {@code options} or {@code context} is {@code null}.
     */
    default Context start(String methodName, StartSpanOptions options, Context context) {
        // fall back to old API if not overriden.
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
     * {@codesnippet com.azure.core.util.tracing.start#string-context-processKind-SEND}
     *
     * <p>Starts a tracing span with provided method name and AMQP operation MESSAGE</p>
     * {@codesnippet com.azure.core.util.tracing.start#string-context-processKind-MESSAGE}
     *
     * <p>Starts a tracing span with provided method name and AMQP operation PROCESS</p>
     * {@codesnippet com.azure.core.util.tracing.start#string-context-processKind-PROCESS}
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @param processKind AMQP operation kind.
     * @return The updated {@link Context} object containing the returned span.
     * @throws NullPointerException if {@code methodName} or {@code context} or {@code processKind} is {@code null}.
     */
    Context start(String methodName, Context context, ProcessKind processKind);

    /**
     * Completes the current tracing span.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Completes the tracing span present in the context, with the corresponding OpenTelemetry status for the given
     * response status code</p>
     * {@codesnippet com.azure.core.util.tracing.end#int-throwable-context}
     *
     * @param responseCode Response status code if the span is in a HTTP call context.
     * @param error {@link Throwable} that happened during the span or {@code null} if no exception occurred.
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code context} is {@code null}.
     */
    void end(int responseCode, Throwable error, Context context);

    /**
     * Completes the current tracing span for AMQP calls.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Completes the tracing span with the corresponding OpenTelemetry status for the given status message</p>
     * {@codesnippet com.azure.core.util.tracing.end#string-throwable-context}
     *
     * @param statusMessage The error or success message that occurred during the call, or {@code null} if no error
     * occurred.
     * @param error {@link Throwable} that happened during the span or {@code null} if no exception occurred.
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code context} is {@code null}.
     */
    void end(String statusMessage, Throwable error, Context context);

    /**
     * Adds metadata to the current span. If no span information is found in the context, then no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code key} or {@code value} or {@code context} is {@code null}.
     */
    void setAttribute(String key, String value, Context context);

    /**
     * Sets the name for spans that are created.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Retrieve the span name of the returned span</p>
     * {@codesnippet com.azure.core.util.tracing.setSpanName#string-context}
     *
     * @param spanName Name to give the next span.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the name of the returned span.
     * @throws NullPointerException if {@code spanName} or {@code context} is {@code null}.
     */
    Context setSpanName(String spanName, Context context);

    /**
     * Provides a way to link multiple tracing spans.
     * Used in batching operations to relate multiple requests under a single batch.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Link multiple spans using their span context information</p>
     * {@codesnippet com.azure.core.util.tracing.addLink#context}
     *
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code context} is {@code null}.
     */
    void addLink(Context context);

    /**
     * Extracts the span's context as {@link Context} from upstream.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Extracts the corresponding span context information from a valid diagnostic id</p>
     * {@codesnippet com.azure.core.util.tracing.extractContext#string-context}
     *
     * @param diagnosticId Unique identifier for the trace information of the span.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the span context.
     * @throws NullPointerException if {@code diagnosticId} or {@code context} is {@code null}.
     */
    Context extractContext(String diagnosticId, Context context);

    /**
     * Returns a span builder with the provided name in {@link Context}.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Returns a builder with the provided span name.</p>
     * {@codesnippet com.azure.core.util.tracing.getSpanBuilder#string-context}
     *
     * @param spanName Name to give the span for the created builder.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the span builder.
     * @throws NullPointerException if {@code context} or {@code spanName} is {@code null}.
     */
    default Context getSharedSpanBuilder(String spanName, Context context) {
        // no-op
        return Context.NONE;
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
    }

    /**
     * Adds an event to the span present in the {@code Context} with the provided {@code timestamp}
     * and {@code attributes}.
     * <p>This API does not provide any normalization if provided timestamps are out of range of the current
     * span timeline</p>
     * <p>Supported attribute values include String, double, boolean, long, String [], double [], long [].
     * Any other Object value type and null values will be silently ignored.</p>
     *
     * @param name the name of the event.
     * @param attributes the additional attributes to be set for the event.
     * @param timestamp The instant, in UTC, at which the event will be associated to the span.
     * @param context the call metadata containing information of the span to which the event should be associated with.
     * @throws NullPointerException if {@code eventName} is {@code null}.
     */
    default void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp,
                          Context context) {

    }
}
