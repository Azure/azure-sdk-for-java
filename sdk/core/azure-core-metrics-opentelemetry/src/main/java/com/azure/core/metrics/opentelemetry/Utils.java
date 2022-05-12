// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

class Utils {
    private static final ClientLogger LOGGER = new ClientLogger(Utils.class);

    /**
     * Adds attribute key-value pair to OpenTelemetry {@link AttributesBuilder}, if value type is not supported by
     * OpenTelemetry, drops the attribute.
     * @param attributesBuilder initialized {@link AttributesBuilder} instance
     * @param key key of the attribute to be added
     * @param value value of the attribute to be added
     */
    static void addAttribute(AttributesBuilder attributesBuilder, String key, Object value) {
        if (value instanceof Boolean) {
            attributesBuilder.put(key, (boolean) value);
        } else if (value instanceof String) {
            attributesBuilder.put(key, String.valueOf(value));
        } else if (value instanceof Double) {
            attributesBuilder.put(key, (Double) value);
        } else if (value instanceof Long) {
            attributesBuilder.put(key, (Long) value);
        } else if (value instanceof String[]) {
            attributesBuilder.put(key, (String[]) value);
        } else if (value instanceof long[]) {
            attributesBuilder.put(key, (long[]) value);
        } else if (value instanceof double[]) {
            attributesBuilder.put(key, (double[]) value);
        } else if (value instanceof boolean[]) {
            attributesBuilder.put(key, (boolean[]) value);
        } else {
            LOGGER.warning("Could not populate attribute with key '{}', type is not supported.");
        }
    }

    /**
     * Returns OpenTelemetry trace context from given com.azure.core.Context under PARENT_TRACE_CONTEXT_KEY.
     * If not context is found, returns {@link io.opentelemetry.context.Context#current()}.
     */
    static io.opentelemetry.context.Context getTraceContextOrCurrent(Context azContext) {
        Optional<Object> traceContextOpt = azContext.getData(PARENT_TRACE_CONTEXT_KEY);
        if (traceContextOpt.isPresent()) {
            Object traceContextObj = traceContextOpt.get();
            if (io.opentelemetry.context.Context.class.isAssignableFrom(traceContextObj.getClass())) {
                return (io.opentelemetry.context.Context) traceContextObj;
            } else {
                LOGGER.warning("Expected {} type under `PARENT_TRACE_CONTEXT_KEY`, but got {}", io.opentelemetry.context.Context.class, traceContextObj.getClass());
            }
        }

        LOGGER.verbose("No context is found under `PARENT_TRACE_CONTEXT_KEY`, getting current context");
        return io.opentelemetry.context.Context.current();
    }
}
