// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.tracing;

import com.azure.core.util.Context;

import java.io.Closeable;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class provides a means for all client libraries to augment the context information they have received from an
 * end user with additional distributed tracing information, that may then be passed on to a backend for analysis.
 */
public final class TracerProxy {

    private static ServiceLoader<? extends Tracer> tracers = ServiceLoader.load(Tracer.class);

    private TracerProxy() {
        // no-op
    }

    /**
     * For each tracer plugged into the SDK a new tracing span is created.
     *
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found the
     * new span will be added as a child, otherwise the span will be created and added to the context and any downstream
     * start calls will use the created span as the parent.
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    public static Context start(String methodName, Context context) {
        Context local = context;
        for (Tracer tracer : tracers) {
            local = tracer.start(methodName, local);
        }

        return local;
    }

    /**
     * For each tracer plugged into the SDK metadata to its current span. The {@code context} is checked for having span
     * information, if no span information is found in the context no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     */
    public static void setAttribute(String key, String value, Context context) {
        tracers.forEach(tracer -> tracer.setAttribute(key, value, context));
    }

    /**
     * For each tracer plugged into the SDK the current tracing span is marked as completed.
     *
     * @param responseCode Response status code if the span is in a HTTP call context.
     * @param error Potential throwable that happened during the span.
     * @param context Additional metadata that is passed through the call stack.
     */
    public static void end(int responseCode, Throwable error, Context context) {
        tracers.forEach(tracer -> tracer.end(responseCode, error, context));
    }

    /**
     * For each tracer plugged into the SDK the span name is set.
     *
     * @param spanName Name of the span.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    public static Context setSpanName(String spanName, Context context) {
        Context local = context;
        for (Tracer tracer : tracers) {
            local = tracer.setSpanName(spanName, context);
        }

        return local;
    }

    /**
     * For each tracer plugged into the SDK the current tracing span is marked as completed.
     *
     * @param errorCondition the AMQP header value for this error condition
     * @param throwable Potential throwable that happened during the span.
     * @param context Additional metadata that is passed through the call stack.
     */
    public static void endAmqp(String errorCondition, Context context, Throwable throwable) {
        tracers.forEach(tracer -> tracer.endAmqp(errorCondition, context, throwable));
    }

    public static void addLink(Context eventContextData) {
        tracers.forEach(tracer -> tracer.addLink(eventContextData));
    }

    public static Context extractContext(String diagnosticId) {
        Context local = Context.NONE;
        for (Tracer tracer : tracers) {
            local = tracer.extractContext(diagnosticId);
        }
        return local;
    }
}
