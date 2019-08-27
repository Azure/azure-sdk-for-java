// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.tracing;

import com.azure.core.util.Context;

/**
 * Contract that all tracers must implement to be plug-able into the SDK.
 */
public interface Tracer {
    /**
     * Key for {@link Context} which indicates that the context contains OpenTelemetry span data. This span will be used
     * as the parent span for all spans the SDK creates.
     *
     * If no span data is listed when the SDK creates its first span it will be used as the parent for all further spans
     * it creates.
     */
    String OPENTELEMETRY_SPAN_KEY = "opentelemetry-span";

    /**
     * Key for {@link Context} which indicates that the context contains the name for the OpenTelemetry spans that are created.
     *
     * If no span name is listed when the span is created it will default to using the calling method's name.
     */
    String OPENTELEMETRY_SPAN_NAME_KEY = "opentelemetry-span-name";

    /**
     * Key for {@link Context} which indicates that the context contains the Entity Path, remote endpoint path.
     *
     */
    String ENTITY_PATH = "entityPath";

    /**
     * Key for {@link Context} which indicates that the context contains the hostname.
     *
     */
    String HOST_NAME = "hostName";

    /**
     * Key for {@link Context} which indicates that the context contains message Span Context.
     *
     */
    String SPAN_CONTEXT = "spanContext";

    /**
     * Key for {@link Context} which indicates that the context contains the Diagnostic Id for the service call.
     *
     */
    String DIAGNOSTIC_ID_KEY = "Diagnostic-Id";

    /**
     * Creates a new tracing span.
     *
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found the
     * new span will be added as a child, otherwise the span will be created and added to the context and any downstream
     * start calls will use the created span as the parent.
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    Context start(String methodName, Context context);

    /**
     * Creates a new tracing span.
     *
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found the
     * new span will be added as a child, otherwise the span will be created and added to the context and any downstream
     * start calls will use the created span as the parent.
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    Context start(String methodName, Context context, ProcessKind processKind);

    /**
     * Completes the current tracing span.
     *
     * @param responseCode Response status code if the span is in a HTTP call context.
     * @param error Potential throwable that happened during the span.
     * @param context Additional metadata that is passed through the call stack.
     */
    void end(int responseCode, Throwable error, Context context);

    /**
     * Completes the current tracing span.
     *
     * @param errorCondition the error message that occurred during the call.
     * @param error Potential throwable that happened during the span.
     * @param context Additional metadata that is passed through the call stack.
     */
    void end(String errorCondition, Throwable error, Context context);

    /**
     * Adds metadata to the current span. The {@code context} is checked for having span information, if no span
     * information is found in the context no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    void setAttribute(String key, String value, Context context);

    /*
     * Adds metadata to the current span. The {@code context} is checked for having span information, if no span
     * information is found in the context no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    // void setAttribute(String key, long value, Context context);

    /*
     * Adds metadata to the current span. The {@code context} is checked for having span information, if no span
     * information is found in the context no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    // void setAttribute(String key, double value, Context context);

    /*
     * Adds metadata to the current span. The {@code context} is checked for having span information, if no span
     * information is found in the context no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    // void setAttribute(String key, boolean value, Context context);

    /**
     * Configures the name for spans that are created.
     *
     * @param spanName Name to give the next span.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    Context setSpanName(String spanName, Context context);

    /**
     * Adds a link to the tracing span.
     * Used in batching operations to relate multiple requests under a single batch.
     *
     * @param context Additional metadata that is passed through the call stack.
     */
    void addLink(Context context);

    /**
     * Extracts the span Context from the given event's diagnostic Id
     *
     * @param diagnosticId Unique identifier for the trace information of the event.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    Context extractContext(String diagnosticId, Context context);
}
