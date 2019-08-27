// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.implementation.tracing.Tracer;
import com.azure.core.util.Context;
import reactor.core.publisher.Signal;

import static com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_SPAN_KEY;

public class TracerProvider {
    private final Iterable<Tracer> tracers;

    public TracerProvider(Iterable<Tracer> tracers) {
        this.tracers = tracers;
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
    public Context startSpan(String methodName, Context context) {
        Context local = context;
        String spanName = "Azure.eventhubs." + methodName;
        for (Tracer tracer : tracers) {
            local = tracer.start(spanName, local);
        }

        return local;
    }

    /**
     * Given a context containing the current tracing span the span is marked completed with status info from {@link Signal}
     * For each tracer plugged into the SDK the current tracing span is marked as completed.
     *
     * @param context Additional metadata that is passed through the call stack.
     * @param signal The signal indicates the status and contains the metadata we need to end the tracing span.
     */
    public void end(Context context, Signal<Void> signal) {
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
        for (Tracer tracer : tracers) {
            tracer.end(errorCondition, throwable, context);
        }
    }

    /**
     * For each tracer plugged into the SDK a link is created between the parent tracing span and
     * the current service call.
     *
     * @param context Additional metadata that is passed through the call stack.
     */
    public void addSpanLinks(Context context) {
        tracers.forEach(tracer -> tracer.addLink(context));
    }

    /**
     * For each tracer plugged into the SDK a new context is extracted from the event's diagnostic Id.
     *
     * @param diagnosticId Unique identifier of an external call from producer to the queue.
     */
    public Context extractContext(String diagnosticId, Context context) {
        Context local = context;
        for (Tracer tracer : tracers) {
            local = tracer.extractContext(diagnosticId, context);
        }
        return local;
    }

    /**
     * For each tracer plugged into the SDK a new scoped tracing span is created.
     *
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found the
     * new span will be added as a child, otherwise the span will be created and added to the context and any downstream
     * start calls will use the created span as the parent.
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    public Context startScopedSpan(String methodName, Context context) {
        Context local = context;
        for (Tracer tracer : tracers) {
            local = tracer.startScopedSpan(methodName, local);
        }

        return local;
    }
}
