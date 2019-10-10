// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.tracing;

import com.azure.core.util.Context;

import java.util.ServiceLoader;

/**
 * This class provides a means for all client libraries to augment the context information they have received from an
 * end user with additional distributed tracing information, that may then be passed on to a backend for analysis.
 *
 * @see Tracer
 */
public final class TracerProxy {

    private static final ServiceLoader<? extends Tracer> TRACERS = ServiceLoader.load(Tracer.class);

    private TracerProxy() {
        // no-op
    }

    /**
     * A new tracing span is created for each {@link Tracer tracer} plugged into the SDK.
     *
     * The {@code context} will be checked for information about a parent span. If a parent span is found, the new span
     * will be added as a child. Otherwise, the parent span will be created and added to the {@code context} and any
     * downstream {@code start()} calls will use the created span as the parent.
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     *
     * @return An updated {@link Context} object.
     */
    public static Context start(String methodName, Context context) {
        Context local = context;
        for (Tracer tracer : TRACERS) {
            local = tracer.start(methodName, local);
        }

        return local;
    }

    /**
     * For each {@link Tracer tracer} plugged into the SDK, the key-value pair metadata is added to its current span. If
     * the {@code context} does not contain a span, then no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    public static void setAttribute(String key, String value, Context context) {
        TRACERS.forEach(tracer -> tracer.setAttribute(key, value, context));
    }

    /**
     * For each {@link Tracer tracer} plugged into the SDK, its current tracing span is marked as completed.
     *
     * @param responseCode Response status code if the span is in a HTTP call context.
     * @param error {@link Throwable} that happened during the span or {@code null} if no exception occurred.
     * @param context Additional metadata that is passed through the call stack.
     */
    public static void end(int responseCode, Throwable error, Context context) {
        TRACERS.forEach(tracer -> tracer.end(responseCode, error, context));
    }

    /**
     * Sets the span name for each {@link Tracer tracer} plugged into the SDK.
     *
     * @param spanName Name of the span.
     * @param context Additional metadata that is passed through the call stack.
     *
     * @return An updated {@link Context} object.
     */
    public static Context setSpanName(String spanName, Context context) {
        Context local = context;
        for (Tracer tracer : TRACERS) {
            local = tracer.setSpanName(spanName, context);
        }

        return local;
    }
}
