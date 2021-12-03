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

    private static Method setSpanKeyMethod;
    private static Object clientSpanKey;

    static {
        agentDiscovered = true;
        try {
            Class<?> spanKeyClass = Class.forName("io.opentelemetry.instrumentation.api.instrumenter.SpanKey");

            clientSpanKey = spanKeyClass.getDeclaredField("ALL_CLIENTS").get(null);
            setSpanKeyMethod = spanKeyClass.getDeclaredMethod("storeInContext", Context.class, Span.class);

            if (setSpanKeyMethod.getReturnType() != Context.class) {
                agentDiscovered = false;
            }

        } catch (Throwable ignored) {
            LOGGER.verbose("Failed to discover OpenTelemetry agent classes, HTTP spans may be duplicated");
            agentDiscovered = false;
        }
    }

    /**
     * Registers span from given trace context as client span for opentelemetry agent to avoid duplication.
     * @param traceContext OpenTelemetry context with client span
     * @return Agent context or null when agent is not running or doesn't behave as expected.
     */
    public static Context registerClientSpan(Context traceContext) {
        Objects.requireNonNull(traceContext, "'traceContext' cannot be null");
        if (agentDiscovered) {
            try {
                return (Context) setSpanKeyMethod.invoke(
                    clientSpanKey,
                    traceContext,
                    Span.fromContext(traceContext));
            } catch (Throwable t) {
                // should not happen, If it does, we'll log it once.
                LOGGER.warning("Failed to register client span on OpenTelemetry agent");
                agentDiscovered = false;
            }
        }
        return traceContext;
    }
}
