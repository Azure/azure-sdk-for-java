package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.DoubleHistogram;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.Map;
import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

public class OpenTelemetryDoubleHistogram implements DoubleHistogram {

    private final io.opentelemetry.api.metrics.DoubleHistogram histogram;
    OpenTelemetryDoubleHistogram(io.opentelemetry.api.metrics.DoubleHistogram histogram) {
        this.histogram = histogram;
    }

    @Override
    public void record(double value, Map<String, Object> attributes, Context context) {
        histogram.record(value, convertToOtelAttributes(attributes), getTraceContextOrDefault(context));
    }


    private Attributes convertToOtelAttributes(Map<String, Object> attributes) {
        AttributesBuilder attributesBuilder = Attributes.builder();
        attributes.forEach((key, value) -> {
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
        });
        return attributesBuilder.build();
    }

    /**
     * Returns OpenTelemetry trace context from given com.azure.core.Context under PARENT_TRACE_CONTEXT_KEY
     * or PARENT_SPAN_KEY (for backward-compatibility) or default value.
     */
    private io.opentelemetry.context.Context getTraceContextOrDefault(Context azContext) {
        io.opentelemetry.context.Context traceContext = getOrNull(azContext,
            PARENT_TRACE_CONTEXT_KEY,
            io.opentelemetry.context.Context.class);

        return traceContext == null ? io.opentelemetry.context.Context.current() : traceContext;
    }

    /**
     * Returns the value of the specified key from the context.
     *
     * @param key The name of the attribute that needs to be extracted from the {@link Context}.
     * @param clazz clazz the type of raw class to find data for.
     * @param context The context containing the specified key.
     * @return The T type of raw class object
     */
    @SuppressWarnings("unchecked")
    private <T> T getOrNull(Context context, String key, Class<T> clazz) {
        final Optional<Object> optional = context.getData(key);
        final Object result = optional.filter(value -> clazz.isAssignableFrom(value.getClass())).orElseGet(() -> {
            //LOGGER.verbose("Could not extract key '{}' of type '{}' from context.", key, clazz);
            return null;
        });

        return (T) result;
    }

}
