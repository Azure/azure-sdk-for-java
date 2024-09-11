// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.util.Context;

import java.time.Instant;
import java.util.function.BiConsumer;

import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.CANCELLED_ERROR_TYPE_VALUE;

/**
 * Helper class that holds state necessary for the operation instrumentation.
 */
public final class InstrumentationScope implements AutoCloseable {
    private final EventHubsMetricsProvider meter;
    private final EventHubsTracer tracer;
    private final boolean isEnabled;
    private final BiConsumer<EventHubsMetricsProvider, InstrumentationScope> reportMetricsCallback;
    private Instant startTime;
    private Throwable error;
    private String errorType;
    private Context span = Context.NONE;
    private AutoCloseable spanScope;
    private boolean closed = false;

    public InstrumentationScope(EventHubsTracer tracer,
                                EventHubsMetricsProvider meter,
                                BiConsumer<EventHubsMetricsProvider, InstrumentationScope> reportMetricsCallback) {
        this.tracer = tracer;
        this.meter = meter;
        this.isEnabled = (tracer != null && tracer.isEnabled()) || (meter != null && meter.isEnabled());
        if (meter != null && meter.isEnabled()) { // micro-optimization
            this.startTime = Instant.now();
        }
        this.reportMetricsCallback = reportMetricsCallback;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public InstrumentationScope setError(Throwable error) {
        if (isEnabled) {
            this.error = error;
        }
        return this;
    }

    public InstrumentationScope setSpan(Context span) {
        if (isEnabled) {
            this.span = span;
        }
        return this;
    }

    public InstrumentationScope setCancelled() {
        // Complicated calls can result in error followed by cancellation. We shouldn't track them twice.
        // don't trust me? try this:
        // Flux.fromIterable(Collections.singletonList("event"))
        //     .flatMap(event -> Mono.error(new RuntimeException("boom"))
        //         .doOnError(e -> System.out.println("Error"))
        //         .doOnCancel(() -> System.out.println("Cancel")))
        //     .blockLast();
        if (isEnabled && error == null) {
            errorType = CANCELLED_ERROR_TYPE_VALUE;
        }

        return this;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Context getSpan() {
        return span;
    }

    public Throwable getError() {
        return error;
    }

    public String getErrorType() {
        if (errorType == null) {
            errorType = InstrumentationUtils.getErrorType(error);
        }
        return errorType;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            if (meter != null && reportMetricsCallback != null) {
                reportMetricsCallback.accept(meter, this);
            }
            if (tracer != null) {
                tracer.endSpan(errorType, error, span, spanScope);
            }
        }
    }

    InstrumentationScope makeSpanCurrent() {
        if (tracer != null && tracer.isEnabled()) {
            spanScope = tracer.makeSpanCurrent(span);
        }
        return this;
    }
}
