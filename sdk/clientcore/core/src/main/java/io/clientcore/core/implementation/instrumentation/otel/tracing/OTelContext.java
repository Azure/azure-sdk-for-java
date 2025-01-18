// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.CONTEXT_KEY_CLASS;

class OTelContext {
    private static final ClientLogger LOGGER = new ClientLogger(OTelContext.class);
    private static final TracingScope NOOP_SCOPE = () -> {
    };
    private static final FallbackInvoker CURRENT_INVOKER;
    private static final FallbackInvoker MAKE_CURRENT_INVOKER;
    private static final FallbackInvoker WITH_INVOKER;
    private static final FallbackInvoker GET_INVOKER;

    // this context key will indicate if the span is created by client core
    // this is used to suppress multiple spans created for the same logical operation
    // such as convenience API on top of protocol methods when both as instrumented.
    // We might need to suppress logical server (consumer) spans in the future, but that
    // was not necessary so far
    private static final Object CLIENT_CORE_SPAN_CONTEXT_KEY;

    static {
        ReflectiveInvoker currentInvoker = null;
        ReflectiveInvoker makeCurrentInvoker = null;
        ReflectiveInvoker withInvoker = null;
        ReflectiveInvoker getInvoker = null;

        Object clientCoreSpanContextKey = null;
        Object rootContext = null;

        if (OTelInitializer.isInitialized()) {
            try {
                currentInvoker = getMethodInvoker(CONTEXT_CLASS, CONTEXT_CLASS.getMethod("current"));
                makeCurrentInvoker = getMethodInvoker(CONTEXT_CLASS, CONTEXT_CLASS.getMethod("makeCurrent"));
                withInvoker
                    = getMethodInvoker(CONTEXT_CLASS, CONTEXT_CLASS.getMethod("with", CONTEXT_KEY_CLASS, Object.class));
                getInvoker = getMethodInvoker(CONTEXT_CLASS, CONTEXT_CLASS.getMethod("get", CONTEXT_KEY_CLASS));

                ReflectiveInvoker contextKeyNamedInvoker
                    = getMethodInvoker(CONTEXT_KEY_CLASS, CONTEXT_KEY_CLASS.getMethod("named", String.class));

                clientCoreSpanContextKey = contextKeyNamedInvoker.invoke("client-core-span");

                ReflectiveInvoker rootInvoker = getMethodInvoker(CONTEXT_CLASS, CONTEXT_CLASS.getMethod("root"));
                rootContext = rootInvoker.invoke();

            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        CURRENT_INVOKER = new FallbackInvoker(currentInvoker, rootContext, LOGGER);
        MAKE_CURRENT_INVOKER = new FallbackInvoker(makeCurrentInvoker, NOOP_SCOPE, LOGGER);
        WITH_INVOKER = new FallbackInvoker(withInvoker, LOGGER);
        GET_INVOKER = new FallbackInvoker(getInvoker, LOGGER);
        CLIENT_CORE_SPAN_CONTEXT_KEY = clientCoreSpanContextKey;
    }

    static Object getCurrent() {
        Object currentContext = CURRENT_INVOKER.invoke();
        assert CONTEXT_CLASS.isInstance(currentContext);
        return currentContext;
    }

    static AutoCloseable makeCurrent(Object context) {
        assert CONTEXT_CLASS.isInstance(context);
        Object scope = MAKE_CURRENT_INVOKER.invoke(context);
        assert scope instanceof AutoCloseable;
        return (AutoCloseable) scope;
    }

    static Object markCoreSpan(Object context, OTelSpan span) {
        assert CONTEXT_CLASS.isInstance(context);
        Object updatedContext = WITH_INVOKER.invoke(context, CLIENT_CORE_SPAN_CONTEXT_KEY, span);
        return updatedContext == null ? context : updatedContext;
    }

    static OTelSpan getClientCoreSpan(Object context) {
        assert CONTEXT_CLASS.isInstance(context);
        Object clientCoreSpan = GET_INVOKER.invoke(context, CLIENT_CORE_SPAN_CONTEXT_KEY);
        assert clientCoreSpan == null || clientCoreSpan instanceof OTelSpan;
        return (OTelSpan) clientCoreSpan;
    }

    /**
     * Get the OpenTelemetry context from the given context.
     *
     * @param context the context
     * @return the OpenTelemetry context
     */
    static Object fromInstrumentationContext(InstrumentationContext context) {
        if (context instanceof OTelSpanContext) {
            Object otelContext = ((OTelSpanContext) context).getOtelContext();
            if (otelContext != null) {
                return otelContext;
            }
        }

        Object currentContext = CURRENT_INVOKER.invoke();
        if (context != null) {
            Object spanContext = OTelSpanContext.toOTelSpanContext(context);
            Object span = OTelSpan.wrapSpanContext(spanContext);

            return OTelSpan.storeInContext(span, currentContext);
        }

        return currentContext;
    }
}
