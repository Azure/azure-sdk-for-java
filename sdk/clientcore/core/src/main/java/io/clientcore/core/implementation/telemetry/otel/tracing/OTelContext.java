// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.tracing.SpanKind;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.CONTEXT_KEY_CLASS;

class OTelContext {
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpan.class);
    private static final ReflectiveInvoker CURRENT_INVOKER;
    private static final ReflectiveInvoker MAKE_CURRENT_INVOKER;
    private static final ReflectiveInvoker WITH_INVOKER;
    private static final ReflectiveInvoker GET_INVOKER;

    // this context key will indicate if the span is created by client core
    // AND has client or internal kind (logical client operation)
    // this is used to suppress multiple spans created for the same logical operation
    // such as convenience API on top of protocol methods when both as instrumented.
    // We might need to suppress logical server (consumer) spans in the future, but that
    // was not necessary so far
    private static final Object HAS_CLIENT_SPAN_CONTEXT_KEY;

    static {
        ReflectiveInvoker currentInvoker = null;
        ReflectiveInvoker makeCurrentInvoker = null;
        ReflectiveInvoker withInvoker = null;
        ReflectiveInvoker getInvoker = null;
        Object hasClientSpanContextKey = null;

        if (OTelInitializer.isInitialized()) {
            try {
                currentInvoker = getMethodInvoker(CONTEXT_CLASS, CONTEXT_CLASS.getMethod("current"));
                makeCurrentInvoker = getMethodInvoker(CONTEXT_CLASS, CONTEXT_CLASS.getMethod("makeCurrent"));
                withInvoker
                    = getMethodInvoker(CONTEXT_CLASS, CONTEXT_CLASS.getMethod("with", CONTEXT_KEY_CLASS, Object.class));
                getInvoker = getMethodInvoker(CONTEXT_CLASS, CONTEXT_CLASS.getMethod("get", CONTEXT_KEY_CLASS));

                ReflectiveInvoker contextKeyNamedInvoker
                    = getMethodInvoker(CONTEXT_KEY_CLASS, CONTEXT_KEY_CLASS.getMethod("named", String.class));

                hasClientSpanContextKey = contextKeyNamedInvoker.invoke("client-core-call");
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        CURRENT_INVOKER = currentInvoker;
        MAKE_CURRENT_INVOKER = makeCurrentInvoker;
        WITH_INVOKER = withInvoker;
        GET_INVOKER = getInvoker;
        HAS_CLIENT_SPAN_CONTEXT_KEY = hasClientSpanContextKey;
    }

    static Object getCurrent() throws Throwable {
        Object currentContext = CURRENT_INVOKER.invoke();
        assert CONTEXT_CLASS.isInstance(currentContext);
        return currentContext;
    }

    static AutoCloseable makeCurrent(Object context) throws Throwable {
        assert CONTEXT_CLASS.isInstance(context);
        Object scope = MAKE_CURRENT_INVOKER.invoke(context);
        assert scope instanceof AutoCloseable;
        return (AutoCloseable) scope;
    }

    static Object markCoreSpan(Object context, SpanKind spanKind) throws Throwable {
        assert CONTEXT_CLASS.isInstance(context);
        if (spanKind == SpanKind.CLIENT || spanKind == SpanKind.INTERNAL) {
            return WITH_INVOKER.invoke(context, HAS_CLIENT_SPAN_CONTEXT_KEY, Boolean.TRUE);
        }
        return context;
    }

    static boolean hasClientCoreSpan(Object context) throws Throwable {
        assert CONTEXT_CLASS.isInstance(context);
        Object flag = GET_INVOKER.invoke(context, HAS_CLIENT_SPAN_CONTEXT_KEY);
        assert flag == null || flag instanceof Boolean;
        return Boolean.TRUE.equals(flag);
    }
}
