// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.logging.ClientLogger;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

public class OpenTelemetryUtils {
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
        } else if (value instanceof Float) {
            attributesBuilder.put(AttributeKey.doubleKey(key), ((Float) value).doubleValue());
        } else if (value instanceof Long) {
            attributesBuilder.put(AttributeKey.longKey(key), (Long) value);
        } else if (value instanceof Integer) {
            attributesBuilder.put(AttributeKey.longKey(key), (Integer) value);
        } else if (value instanceof Short) {
            attributesBuilder.put(AttributeKey.longKey(key), (Short) value);
        } else if (value instanceof Byte) {
            attributesBuilder.put(AttributeKey.longKey(key), (Byte) value);
        } else if (value instanceof String[]) {
            attributesBuilder.put(AttributeKey.stringArrayKey(key), (String[]) value);
        } else if (value instanceof Long[]) {
            attributesBuilder.put(AttributeKey.longArrayKey(key), (Long[]) value);
        } else if (value instanceof long[]) {
            long[] array = (long[]) value;
            List<Long> boxed = new ArrayList<>(array.length);
            for (int i = 0; i < array.length; i++) {
                boxed.add(array[i]);
            }
            attributesBuilder.put(AttributeKey.longArrayKey(key), boxed);
        } else if (value instanceof double[]) {
            double[] array = (double[]) value;
            List<Double> boxed = new ArrayList<>(array.length);
            for (int i = 0; i < array.length; i++) {
                boxed.add(array[i]);
            }
            attributesBuilder.put(AttributeKey.doubleArrayKey(key), boxed);
        } else if (value instanceof boolean[]) {
            boolean[] array = (boolean[]) value;
            List<Boolean> boxed = new ArrayList<>(array.length);
            for (int i = 0; i < array.length; i++) {
                boxed.add(array[i]);
            }
            attributesBuilder.put(AttributeKey.booleanArrayKey(key), boxed);
        } else {
            LOGGER.warning("Could not populate attribute with key '{}', type {} is not supported.", key, value.getClass().getName());
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
                LOGGER.warning("Expected instance of `io.opentelemetry.context.Context` under `PARENT_TRACE_CONTEXT_KEY`, but got {}, ignoring it.", traceContextObj.getClass().getName());
            }
        }
        return io.opentelemetry.context.Context.current();
    }

    static Attributes getAttributes(TelemetryAttributes attributesBuilder) {
        if (attributesBuilder instanceof OpenTelemetryAttributes) {
            return ((OpenTelemetryAttributes) attributesBuilder).get();
        }

        if (attributesBuilder != null) {
            LOGGER.warning("Expected instance of `OpenTelemetryAttributeBuilder` in `attributeCollection`, but got {}, ignoring it.", attributesBuilder.getClass().getName());
        }

        return Attributes.empty();
    }

    /**
     * Parses an OpenTelemetry Status from AMQP Error Condition.
     *
     * @param span the span to set the status for.
     * @param statusMessage description for this error condition.
     * @param throwable the error occurred during response transmission (optional).
     * @return the corresponding OpenTelemetry {@link Span}.
     */
    public static Span setStatus(Span span, String statusMessage, Throwable throwable) {
        if (throwable != null) {
            span.recordException(throwable);
            return span.setStatus(StatusCode.ERROR);
        }
        if (statusMessage != null) {
            if ("success".equalsIgnoreCase(statusMessage)) {
                return span.setStatus(StatusCode.OK);
            } else if ("error".equalsIgnoreCase(statusMessage)) {
                return span.setStatus(StatusCode.ERROR);
            }
        }

        return span.setStatus(StatusCode.UNSET, statusMessage);
    }

    public static Span setStatus(Span span, int httpStatusCode, Throwable throwable) {
        if (throwable != null) {
            span.recordException(throwable);
            return span.setStatus(StatusCode.ERROR);
        }

        return span.setStatus(httpStatusCode >= 400 ? StatusCode.ERROR : StatusCode.UNSET);
    }
}
