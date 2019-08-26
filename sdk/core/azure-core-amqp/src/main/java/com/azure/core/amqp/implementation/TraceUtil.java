// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.implementation.tracing.TracerProxy;
import com.azure.core.util.Context;
import reactor.core.publisher.Signal;

import static com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_SPAN_KEY;

/**
 * Helper class to help start tracing spans.
 */
public class TraceUtil {

    // So this class can't be instantiated.
    private TraceUtil() {
    }

    /**
     * Starts the tracing span for the current service call.
     *
     * @param context Context information about the current service call.
     * @return The updated context containing the span context.
     */
    public static Context start(String methodName, Context context) {
        String spanName = "Azure.eventhubs." + methodName;
        context = TracerProxy.setSpanName(spanName, context);
        return TracerProxy.start(spanName, context);
    }

    /**
     * Starts a new scoped tracing span for the current service call.
     *
     * @param context Context information about the current service call.
     * @return The updated context containing the span context.
     */
    public static Context startScopedSpan(String methodName, Context context) {
        String spanName = "Azure.eventhubs." + methodName;
        context = TracerProxy.setSpanName(spanName, context);
        return TracerProxy.startScopedSpan(spanName, context);
    }

    /**
     * Given a context containing the current tracing span the span is marked completed with status info from {@link Signal}
     *
     * @param context Additional metadata that is passed through the call stack.
     * @param signal The signal indicates the status and contains the metadata we need to end the tracing span.
     */
    public static void endTracingSpan(Context context, Signal<Void> signal) {
        String errorCondition = "";

        // Get the context that was added to the mono, this will contain the information needed to end the span.
        if (!context.getData(OPENTELEMETRY_SPAN_KEY).isPresent()) {
            return;
        }

        Throwable throwable = null;
        if (signal != null && signal.hasError()) {
            // The last status available is on error, this contains the thrown error.
            throwable = signal.getThrowable();

            if (throwable instanceof AmqpException) {
                AmqpException exception = (AmqpException) throwable;
                errorCondition = exception.getErrorCondition().getErrorCondition();
            }
        }
        TracerProxy.end(errorCondition, context, throwable);
    }

    public static void addSpanLinks(Context eventContextData) {
        TracerProxy.addLink(eventContextData);
    }

    public static Context extractContext(String diagnosticId, Context context) {
        return TracerProxy.extractContext(diagnosticId, context);
    }
}
