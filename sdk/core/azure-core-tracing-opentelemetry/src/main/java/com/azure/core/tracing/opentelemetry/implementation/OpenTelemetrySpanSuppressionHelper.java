// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry.implementation;

import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Helper allowing to register CLIENT spans to suppress nested auto-collected CLIENT spans.
 * Currently it's done through reflection, long-term solution for opentelemetry-api
 * is being under development https://github.com/open-telemetry/oteps/pull/172
 */
public class OpenTelemetrySpanSuppressionHelper {
    private static boolean useReflection;
    private static final ClientLogger LOGGER = new ClientLogger(OpenTelemetrySpanSuppressionHelper.class);
    private static Method getAgentContextMethod;
    private static Method setSpanKeyMethod;
    private static Object clientSpanKey;
    private static Method getAgentSpanMethod;
    private static Method agentContextMakeCurrentMethod;

    static {
        useReflection = true;
        try {
            Class<?> agentContextStorageClass = Class.forName("io.opentelemetry.javaagent.instrumentation.opentelemetryapi.context.AgentContextStorage");
            Class<?> shadedContextClass = Class.forName("io.opentelemetry.javaagent.shaded.io.opentelemetry.context.Context");
            Class<?> spanKeyClass = Class.forName("io.opentelemetry.javaagent.shaded.instrumentation.api.instrumenter.SpanKey");
            Class<?> bridgingClass = Class.forName("io.opentelemetry.javaagent.instrumentation.opentelemetryapi.trace.Bridging");
            Class<?> spanShadedClass = Class.forName("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span");

            getAgentContextMethod = agentContextStorageClass.getDeclaredMethod("getAgentContext", io.opentelemetry.context.Context.class);
            getAgentSpanMethod = bridgingClass.getDeclaredMethod("toAgentOrNull", io.opentelemetry.api.trace.Span.class);
            clientSpanKey = spanKeyClass.getDeclaredField("ALL_CLIENTS").get(null);
            setSpanKeyMethod = spanKeyClass.getDeclaredMethod("storeInContext", shadedContextClass, spanShadedClass);
            agentContextMakeCurrentMethod = shadedContextClass.getMethod("makeCurrent");

            if (getAgentContextMethod.getReturnType() != shadedContextClass
                || getAgentSpanMethod.getReturnType() != spanShadedClass
                || setSpanKeyMethod.getReturnType() != shadedContextClass
                || !AutoCloseable.class.isAssignableFrom(agentContextMakeCurrentMethod.getReturnType())) {
                useReflection = false;
            }

        } catch (Throwable ignored) {
            useReflection = false;
            LOGGER.verbose("Failed to discover opentelemetry classed, HTTP spans may be duplicated");
        }
    }

    /**
     * Registers span from given trace context as client span for opentelemetry agent to avoid duplication.
     * @param traceContext OpenTelemetry context with client span
     * @return Agent context or null when agent is not running or doesn't behave as expected.
     */
    public static Object registerClientSpan(Context traceContext) {
        Objects.requireNonNull(traceContext, "'traceContext' cannot be null");
        if (useReflection) {
            try {
                return setSpanKeyMethod.invoke(
                    clientSpanKey,
                    getAgentContextMethod.invoke(null, traceContext),
                    getAgentSpanMethod.invoke(null, Span.fromContext(traceContext)));
            } catch (Throwable t) {
                useReflection = false;
            }
        }
        return null;
    }

    /**
     * Makes agent context current. Falls back to normal trace context when agent is not running
     * or doesn't behave as expected.
     * @param agentContext agent context instance obtained from {@link OpenTelemetrySpanSuppressionHelper#registerClientSpan}
     * @param traceContext OpenTelemetry context to fallback to.
     * @return
     */
    public static AutoCloseable makeCurrent(Object agentContext, Context traceContext) {
        Objects.requireNonNull(traceContext, "'traceContext' cannot be null");
        if (useReflection && agentContext != null) {
            try {
                return (AutoCloseable) agentContextMakeCurrentMethod.invoke(agentContext);
            } catch (Throwable t) {
                useReflection = false;
            }
        }

        return traceContext.makeCurrent();
    }
}
