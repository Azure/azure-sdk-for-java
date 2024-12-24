// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.observability.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.observability.tracing.SpanBuilder;
import io.clientcore.core.observability.tracing.Tracer;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.TRACER_CLASS;

public class OTelTracer implements Tracer {
    public static final OTelTracer NOOP = new OTelTracer(null);
    private static final ClientLogger LOGGER = new ClientLogger(OTelTracer.class);
    private static final ReflectiveInvoker SPAN_BUILDER_INVOKER;
    private final Object otelTracer;

    static {
        ReflectiveInvoker spanBuilderInvoker = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                spanBuilderInvoker = ReflectionUtils.getMethodInvoker(TRACER_CLASS,
                    TRACER_CLASS.getMethod("spanBuilder", String.class));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        SPAN_BUILDER_INVOKER = spanBuilderInvoker;
    }

    OTelTracer(Object otelTracer) {
        this.otelTracer = otelTracer;
    }

    @Override
    public SpanBuilder spanBuilder(String spanName) {
        if (isEnabled()) {
            try {
                return new OTelSpanBuilder(SPAN_BUILDER_INVOKER.invokeWithArguments(otelTracer, spanName));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return OTelSpanBuilder.NOOP;
    }

    @Override
    public boolean isEnabled() {
        return OTelInitializer.INSTANCE.isInitialized() && otelTracer != null;
    }
}
