// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpan;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpanContext;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;

/**
 * This class provides access to the OpenTelemetry context.
 */
public class OTelContext {
    private static final ClientLogger LOGGER = new ClientLogger(OTelContext.class);
    private static final TracingScope NOOP_SCOPE = () -> {
    };

    // this context key will indicate if the span is created by client core
    // this is used to suppress multiple spans created for the same logical operation
    // such as convenience API on top of protocol methods when both as instrumented.
    // We might need to suppress logical server (consumer) spans in the future, but that
    // was not necessary so far
    private static final ContextKey<OTelSpan> CLIENT_CORE_SPAN_CONTEXT_KEY;

    private static final Object ROOT_CONTEXT;

    static {
        ContextKey<OTelSpan> clientCoreSpanContextKey = null;
        Object rootContext = null;

        if (OTelInitializer.isInitialized()) {
            try {
                clientCoreSpanContextKey = ContextKey.named("client-core-span");

                rootContext = Context.root();
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        ROOT_CONTEXT = rootContext;
        CLIENT_CORE_SPAN_CONTEXT_KEY = clientCoreSpanContextKey;
    }

    /**
     * Get the current OpenTelemetry context.
     * @return the current OpenTelemetry context
     */
    public static Object getCurrent() {
        return OTelInitializer.isInitialized()
            ? FallbackInvoker.call(LOGGER, ROOT_CONTEXT, () -> Context.current())
            : ROOT_CONTEXT;
    }

    /**
     * Make the given context the current context.
     *
     * @param context the context
     * @return an AutoCloseable that will restore the previous context when closed
     */
    public static AutoCloseable makeCurrent(Object context) {
        return OTelInitializer.isInitialized()
            ? FallbackInvoker.call(LOGGER, NOOP_SCOPE, () -> ((Context) context).makeCurrent())
            : NOOP_SCOPE;
    }

    /**
     * Mark the given context with the given core span.
     * @param context the context
     * @param span the core span
     * @return the updated context
     */
    public static Object markCoreSpan(Object context, OTelSpan span) {
        Object updatedContext
            = FallbackInvoker.call(LOGGER, null, () -> ((Context) context).with(CLIENT_CORE_SPAN_CONTEXT_KEY, span));
        return updatedContext == null ? context : updatedContext;
    }

    /**
     * Get the core span from the given context.
     * @param context the context
     * @return the core span
     */
    public static OTelSpan getClientCoreSpan(Object context) {
        return FallbackInvoker.call(LOGGER, null, () -> ((Context) context).get(CLIENT_CORE_SPAN_CONTEXT_KEY));
    }

    /**
     * Get the OpenTelemetry context from the given context.
     *
     * @param context the context
     * @return the OpenTelemetry context
     */
    public static Object fromInstrumentationContext(InstrumentationContext context) {
        if (context instanceof OTelSpanContext) {
            Object otelContext = ((OTelSpanContext) context).getOtelContext();
            if (otelContext != null) {
                return otelContext;
            }
        }

        Object currentContext = getCurrent();
        if (context != null) {
            Object spanContext = OTelSpanContext.toOTelSpanContext(context);
            Object span = OTelSpan.wrapSpanContext(spanContext);

            return OTelSpan.storeInContext(span, currentContext);
        }

        return currentContext;
    }
}
