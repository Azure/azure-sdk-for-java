// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry.implementation;

import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Helper allowing to register CLIENT spans to suppress nested auto-collected CLIENT spans
 * and propagate context to lower levels of instrumentation or logs.
 * Currently it's done through reflection against OpenTelemetry instrumentation-api in the agent.
 * long-term solution for opentelemetry-api is under development https://github.com/open-telemetry/oteps/pull/172
 */
public class OpenTelemetrySpanSuppressionHelper {
    private static boolean agentDiscovered;
    private static final ClientLogger LOGGER = new ClientLogger(OpenTelemetrySpanSuppressionHelper.class);
    private static Method getAgentContextMethod;
    private static Method setSpanKeyMethod;
    private static Object clientSpanKey;
    private static Method getAgentSpanMethod;
    private static Method agentContextMakeCurrentMethod;

    static {
        agentDiscovered = true;
        try {
            Class<?> agentContextStorageClass = Class.forName("io.opentelemetry.javaagent.instrumentation.opentelemetryapi.context.AgentContextStorage");
            Class<?> agentContextClass = Class.forName("io.opentelemetry.javaagent.shaded.io.opentelemetry.context.Context");
            Class<?> spanKeyClass = Class.forName("io.opentelemetry.javaagent.shaded.instrumentation.api.instrumenter.SpanKey");
            Class<?> bridgingClass = Class.forName("io.opentelemetry.javaagent.instrumentation.opentelemetryapi.trace.Bridging");
            Class<?> agentSpanClass = Class.forName("io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span");

            getAgentContextMethod = agentContextStorageClass.getDeclaredMethod("getAgentContext", io.opentelemetry.context.Context.class);
            getAgentSpanMethod = bridgingClass.getDeclaredMethod("toAgentOrNull", io.opentelemetry.api.trace.Span.class);
            clientSpanKey = spanKeyClass.getDeclaredField("ALL_CLIENTS").get(null);
            setSpanKeyMethod = spanKeyClass.getDeclaredMethod("storeInContext", agentContextClass, agentSpanClass);
            agentContextMakeCurrentMethod = agentContextClass.getMethod("makeCurrent");

            if (getAgentContextMethod.getReturnType() != agentContextClass
                || getAgentSpanMethod.getReturnType() != agentSpanClass
                || setSpanKeyMethod.getReturnType() != agentContextClass
                || !AutoCloseable.class.isAssignableFrom(agentContextMakeCurrentMethod.getReturnType())) {
                agentDiscovered = false;
            }

        } catch (Exception ex) {
            LOGGER.verbose("Failed to discover OpenTelemetry agent classes, HTTP spans may be duplicated", ex);
            agentDiscovered = false;
        }
    }

    /**
     * Registers span from given trace context as client span for opentelemetry agent to avoid duplication.
     * @param traceContext OpenTelemetry context with client span
     * @return Agent context or null when agent is not running or doesn't behave as expected.
     */
    public static Object registerClientSpan(Context traceContext) {
        Objects.requireNonNull(traceContext, "'traceContext' cannot be null");
        if (agentDiscovered) {
            try {
                return setSpanKeyMethod.invoke(
                    clientSpanKey,
                    getAgentContextMethod.invoke(null, traceContext),
                    getAgentSpanMethod.invoke(null, Span.fromContext(traceContext)));
            } catch (Exception ex) {
                // should not happen, If it does, we'll log it once.
                LOGGER.warning("Failed to register client span on OpenTelemetry agent", ex);
                agentDiscovered = false;
            }
        }
        return null;
    }

    /**
     * Makes passed agent context current. Falls back to passed trace context when agent is not running
     * or doesn't behave as expected.
     * @param agentContext agent context instance obtained from {@link OpenTelemetrySpanSuppressionHelper#registerClientSpan}
     * @param traceContext Regular OpenTelemetry context to fallback to.
     * @return scope to be closed in the same thread as it was started.
     */
    public static AutoCloseable makeCurrent(Object agentContext, Context traceContext) {
        Objects.requireNonNull(traceContext, "'traceContext' cannot be null");
        if (agentDiscovered && agentContext != null) {
            try {
                return (AutoCloseable) agentContextMakeCurrentMethod.invoke(agentContext);
            } catch (Exception ex) {
                // should not happen, If it does, we'll log it once.
                LOGGER.warning("Failed to make OpenTelemetry agent context current", ex);
                agentDiscovered = false;
            }
        }

        return traceContext.makeCurrent();
    }
}
