// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.implementation.tracing.ProcessKind;
import com.azure.core.implementation.tracing.Tracer;
import com.azure.core.util.Context;
import reactor.core.publisher.Signal;

import java.util.Objects;

import static com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_SPAN_KEY;

public class TracerProvider {
    private final Iterable<Tracer> tracers;

    public TracerProvider(Iterable<Tracer> tracers) {
        this.tracers = Objects.requireNonNull(tracers, "'tracers' cannot be null.");
    }

    /**
     * For each tracer plugged into the SDK a new tracing span is created.
     *
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found the
     * new span will be added as a child, otherwise the span will be created and added to the context and any downstream
     * start calls will use the created span as the parent.
     *
     * @param {@link Context context} Additional metadata that is passed through the call stack.
     * @param {@link ProcessKind processKind} the invoking process type.
     * @return An updated context object.
     */
    public Context startSpan(Context context, ProcessKind processKind) {
        Objects.requireNonNull(context, "'context' cannot be null");
        Objects.requireNonNull(processKind, "'processKind' cannot be null");
        Context local = context;
        String spanName = "Azure.eventhubs." + processKind.getProcessKind();
        for (Tracer tracer : tracers) {
            local = tracer.start(spanName, local, processKind);
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
    public void endSpan(Context context, Signal<Void> signal) {

        // Get the context that was added to the mono, this will contain the information needed to end the span.
        if (context != null && !context.getData(OPENTELEMETRY_SPAN_KEY).isPresent()) {
            return;
        }

        if (signal.isOnComplete()) {
            end("success", null, context);
            return;
        }

        String errorCondition = "";
        Throwable throwable = null;
        if (signal.hasError()) {
            // The last status available is on error, this contains the thrown error.
            throwable = signal.getThrowable();

            if (throwable instanceof AmqpException) {
                AmqpException exception = (AmqpException) throwable;
                errorCondition = exception.getErrorCondition().getErrorCondition();
            }
        }
        end(errorCondition, throwable, context);
    }

    /**
     * For each tracer plugged into the SDK a link is created between the parent tracing span and
     * the current service call.
     *
     * @param context Additional metadata that is passed through the call stack.
     */
    public void addSpanLinks(Context context) {
        Objects.requireNonNull(context, "'context' cannot be null");
        tracers.forEach(tracer -> tracer.addLink(context));
    }

    /**
     * For each tracer plugged into the SDK a new context is extracted from the event's diagnostic Id.
     *
     * @param diagnosticId Unique identifier of an external call from producer to the queue.
     */
    public Context extractContext(String diagnosticId, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null");
        Objects.requireNonNull(diagnosticId, "'diagnosticId' cannot be null");
        Context local = context;
        for (Tracer tracer : tracers) {
            local = tracer.extractContext(diagnosticId, local);
        }
        return local;
    }

    private void end(String statusMessage, Throwable throwable, Context context) {
        for (Tracer tracer : tracers) {
            tracer.end(statusMessage, throwable, context);
        }
    }
}
