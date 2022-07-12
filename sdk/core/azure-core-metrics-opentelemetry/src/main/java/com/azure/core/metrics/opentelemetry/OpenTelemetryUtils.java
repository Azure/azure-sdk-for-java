// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

class OpenTelemetryUtils {
    private static final ClientLogger LOGGER = new ClientLogger(OpenTelemetryUtils.class);

    /**
     * Adds attribute key-value pair to OpenTelemetry {@link io.opentelemetry.api.common.AttributesBuilder}, if value type is not supported by
     * OpenTelemetry, drops the attribute.
     * @param attributesBuilder initialized {@link io.opentelemetry.api.common.AttributesBuilder} instance
     * @param key key of the attribute to be added
     * @param value value of the attribute to be added
     */
    static void addAttribute(io.opentelemetry.api.common.AttributesBuilder attributesBuilder, String key, Object value) {
        if (value instanceof Boolean) {
            attributesBuilder.put(AttributeKey.booleanKey(key), (Boolean) value);
        } else if (value instanceof String) {
            attributesBuilder.put(AttributeKey.stringKey(key), (String) value);
        } else if (value instanceof Double) {
            attributesBuilder.put(AttributeKey.doubleKey(key), (Double) value);
        } else if (value instanceof Long) {
            attributesBuilder.put(AttributeKey.longKey(key), (Long) value);
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
            if (traceContextObj instanceof io.opentelemetry.context.Context) {
                return (io.opentelemetry.context.Context) traceContextObj;
            } else if (traceContextObj != null) {
                LOGGER.warning("Expected instance of `io.opentelemetry.context.Context` under `PARENT_TRACE_CONTEXT_KEY`, but got {}, ignoring it.", traceContextObj.getClass());
            }
        }

        LOGGER.verbose("No context is found under `PARENT_TRACE_CONTEXT_KEY`, getting current context");
        return io.opentelemetry.context.Context.current();
    }

    static Attributes getAttributes(TelemetryAttributes attributesBuilder) {
        if (attributesBuilder instanceof OpenTelemetryAttributes) {
            return ((OpenTelemetryAttributes) attributesBuilder).get();
        }

        if (attributesBuilder != null) {
            LOGGER.warning("Expected instance of `OpenTelemetryAttributeBuilder` in `attributeCollection`, but got {}, ignoring it.", attributesBuilder.getClass());
        }

        return Attributes.empty();
    }
}
