package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

class AttributeHelper {
    static void convertToOtelAttribute(AttributesBuilder attributesBuilder, String key, Object value) {
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
            //LOGGER.warning("Could not populate attribute with key '{}', type is not supported.");
        }
    }

    /**
     * Returns OpenTelemetry trace context from given com.azure.core.Context under PARENT_TRACE_CONTEXT_KEY
     * or PARENT_SPAN_KEY (for backward-compatibility) or default value.
     */
    static io.opentelemetry.context.Context getTraceContextOrDefault(Context azContext) {
        Optional<Object> traceContextOpt = azContext.getData(PARENT_TRACE_CONTEXT_KEY);
        if (traceContextOpt.isPresent()) {
            Object traceContextObj = traceContextOpt.get();
            if (io.opentelemetry.context.Context.class.isAssignableFrom(traceContextObj.getClass())) {
                return (io.opentelemetry.context.Context) traceContextObj;
            }
        }

        return io.opentelemetry.context.Context.current();
    }
}
