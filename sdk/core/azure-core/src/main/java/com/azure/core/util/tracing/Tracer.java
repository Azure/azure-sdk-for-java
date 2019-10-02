// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import com.azure.core.util.Context;

/**
 * Contract that all tracers must implement to be plug-able into the SDK.
 *
 * @see Tracer
 */
public interface Tracer {
    /**
     * Key for {@link Context} which indicates that the context contains OpenCensus span data. This span will be used
     * as the parent span for all spans the SDK creates.
     *
     * If no span data is listed when the SDK creates its first span, this span key will be used as the parent span.
     */
    String OPENCENSUS_SPAN_KEY = "opencensus-span";

    /**
     * Key for {@link Context} which indicates that the context contains the name for the OpenCensus spans that are
     * created.
     *
     * If no span name is listed when the span is created it will default to using the calling method's name.
     */
    String OPENCENSUS_SPAN_NAME_KEY = "opencensus-span-name";

    /**
     * Key for {@link Context} which indicates that the context contains an entity path.
     */
    String ENTITY_PATH = "entity-path";

    /**
     * Key for {@link Context} which indicates that the context contains the hostname.
     *
     */
    String HOST_NAME = "hostname";

    /**
     * Key for {@link Context} which indicates that the context contains a message span context.
     *
     */
    String SPAN_CONTEXT = "span-context";

    /**
     * Key for {@link Context} which indicates that the context contains a "Diagnostic Id" for the service call.
     *
     */
    String DIAGNOSTIC_ID_KEY = "diagnostic-id";

    /**
     * Creates a new tracing span.
     *
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found, the
     * new span will be added as a child. Otherwise the parent span will be created and added to the {@code context}
     * and any downstream {@code start()} calls will use the created span as the parent.
     *
     * <p><strong>Code samples</strong></p>
     * <p>Starts a tracing span with provided method name and explicit parent span</p>
     * {@codesnippet com.azure.core.util.tracing.start#string-context}
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     *
     * @return The updated {@link Context} object containing the returned span.
     * @throws NullPointerException if {@code methodName} or {@code context} is {@code null}.
     */
    Context start(String methodName, Context context);

    /**
     * Creates a new tracing span for AMQP calls.
     *
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found the
     * new span will be added as a child. Otherwise the span will be created and added to the {@code context} and any
     * downstream {@code start()} calls will use the created span as the parent.
     *
     * Sets additional request attributes on the created span for the given {@link ProcessKind} SEND.
     * Returns the diagnostic Id and span context of the returned span for the given {@link ProcessKind} RECEIVE.
     * Creates a new tracing span with remote parent and returns that scope when the given {@link ProcessKind} PROCESS.
     *
     * <p><strong>Code samples</strong></p>
     * <p>Starts a tracing span with provided method name and AMQP operation SEND</p>
     * {@codesnippet com.azure.core.util.tracing.start#string-context-processKind-SEND}
     *
     * <p>Starts a tracing span with provided method name and AMQP operation RECEIVE</p>
     * {@codesnippet com.azure.core.util.tracing.start#string-context-processKind-RECEIVE}
     *
     * <p>Starts a tracing span with provided method name and AMQP operation PROCESS</p>
     * {@codesnippet com.azure.core.util.tracing.start#string-context-processKind-PROCESS}
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @param processKind AMQP operation kind.
     *
     * @return The updated {@link Context} object containing the returned span.
     * @throws NullPointerException if {@code methodName} or {@code context} or {@code processKind} is {@code null}.
     */
    Context start(String methodName, Context context, ProcessKind processKind);

    /**
     * Completes the current tracing span.
     *
     * <p><strong>Code samples</strong></p>
     * <p>Completes the tracing span present in the context, with the corresponding OpenCensus status for the given
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
     * Completes the current tracing span.
     *
     * <p><strong>Code samples</strong></p>
     * <p>Completes the tracing span with the corresponding OpenCensus status for the given status message</p>
     * {@codesnippet com.azure.core.util.tracing.end#string-throwable-context}
     *
     * @param statusMessage the error or success message hat occurred during the call, or {@code null} if no error occurred.
     * @param error {@link Throwable} that happened during the span or {@code null} if no exception occurred.
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code context} is {@code null}.
     */
    void end(String statusMessage, Throwable error, Context context);

    /**
     * Adds metadata to the current span. The {@code context} is checked for having span information, if no span
     * information is found in the context no metadata is added.
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
     * <p>Retrieve the span name of the returned span</p>
     * {@codesnippet com.azure.core.util.tracing.setSpanName#string-context}
     *
     * @param spanName Name to give the next span.
     * @param context Additional metadata that is passed through the call stack.
     *
     * @return The updated {@link Context} object containing the name of the returned span.
     * @throws NullPointerException if {@code spanName} or {@code context} is {@code null}.
     */
    Context setSpanName(String spanName, Context context);

    /**
     * Provides a way to link multiple tracing spans.
     * Used in batching operations to relate multiple requests under a single batch.
     *
     * <p><strong>Code samples</strong></p>
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
     * <p>Extracts the corresponding span context information from a valid diagnostic id</p>
     * {@codesnippet com.azure.core.util.tracing.extractContext#string-context}
     *
     * @param diagnosticId Unique identifier for the trace information of the span.
     * @param context Additional metadata that is passed through the call stack.
     *
     * @return The updated {@link Context} object containing the span context.
     * @throws NullPointerException if {@code diagnosticId} or {@code context} is {@code null}.
     */
    Context extractContext(String diagnosticId, Context context);
}
