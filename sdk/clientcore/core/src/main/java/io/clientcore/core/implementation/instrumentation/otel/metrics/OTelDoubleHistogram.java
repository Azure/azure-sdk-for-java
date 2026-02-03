// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.metrics;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelAttributes;
import io.clientcore.core.implementation.instrumentation.otel.OTelContext;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;

import java.util.List;
import java.util.Objects;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTES_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.DOUBLE_HISTOGRAM_BUILDER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.DOUBLE_HISTOGRAM_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.METER_CLASS;

final class OTelDoubleHistogram implements DoubleHistogram {
    private static final DoubleHistogram NOOP = new OTelDoubleHistogram(null);
    private static final ClientLogger LOGGER = new ClientLogger(OTelDoubleHistogram.class);
    private static final FallbackInvoker RECORD_INVOKER;
    private static final FallbackInvoker HISTOGRAM_BUILDER_INVOKER;
    private static final FallbackInvoker SET_DESCRIPTION_INVOKER;
    private static final FallbackInvoker SET_UNIT_INVOKER;
    private static final FallbackInvoker BUILD_INVOKER;
    private static final FallbackInvoker SET_EXPLICIT_BUCKET_BOUNDARIES_INVOKER;

    private final Object otelHistogram;

    static {
        ReflectiveInvoker recordInvoker = null;
        ReflectiveInvoker histogramBuilderInvoker = null;
        ReflectiveInvoker setDescriptionInvoker = null;
        ReflectiveInvoker setUnitInvoker = null;
        ReflectiveInvoker buildInvoker = null;
        ReflectiveInvoker setExplicitBucketBoundariesInvoker = null;

        if (OTelInitializer.isInitialized()) {
            try {
                histogramBuilderInvoker
                    = getMethodInvoker(METER_CLASS, METER_CLASS.getMethod("histogramBuilder", String.class));

                setDescriptionInvoker = getMethodInvoker(DOUBLE_HISTOGRAM_BUILDER_CLASS,
                    DOUBLE_HISTOGRAM_BUILDER_CLASS.getMethod("setDescription", String.class));

                setUnitInvoker = getMethodInvoker(DOUBLE_HISTOGRAM_BUILDER_CLASS,
                    DOUBLE_HISTOGRAM_BUILDER_CLASS.getMethod("setUnit", String.class));

                buildInvoker = getMethodInvoker(DOUBLE_HISTOGRAM_BUILDER_CLASS,
                    DOUBLE_HISTOGRAM_BUILDER_CLASS.getMethod("build"));

                recordInvoker = getMethodInvoker(DOUBLE_HISTOGRAM_CLASS,
                    DOUBLE_HISTOGRAM_CLASS.getMethod("record", double.class, ATTRIBUTES_CLASS, CONTEXT_CLASS));

                setExplicitBucketBoundariesInvoker = getMethodInvoker(DOUBLE_HISTOGRAM_BUILDER_CLASS,
                    DOUBLE_HISTOGRAM_BUILDER_CLASS.getMethod("setExplicitBucketBoundariesAdvice", List.class));
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        HISTOGRAM_BUILDER_INVOKER = new FallbackInvoker(histogramBuilderInvoker, LOGGER);
        SET_DESCRIPTION_INVOKER = new FallbackInvoker(setDescriptionInvoker, LOGGER);
        SET_UNIT_INVOKER = new FallbackInvoker(setUnitInvoker, LOGGER);
        BUILD_INVOKER = new FallbackInvoker(buildInvoker, LOGGER);
        RECORD_INVOKER = new FallbackInvoker(recordInvoker, LOGGER);
        SET_EXPLICIT_BUCKET_BOUNDARIES_INVOKER = new FallbackInvoker(setExplicitBucketBoundariesInvoker, LOGGER);
    }

    private OTelDoubleHistogram(Object otelHistogram) {
        this.otelHistogram = otelHistogram;
    }

    public static DoubleHistogram create(Object otelMeter, String name, String description, String unit,
        List<Double> bucketBoundaries) {
        if (otelMeter == null || !OTelInitializer.isInitialized()) {
            return NOOP;
        }

        Object histogramBuilder = HISTOGRAM_BUILDER_INVOKER.invoke(otelMeter, name);
        SET_DESCRIPTION_INVOKER.invoke(histogramBuilder, description);
        SET_UNIT_INVOKER.invoke(histogramBuilder, unit);
        if (bucketBoundaries != null) {
            SET_EXPLICIT_BUCKET_BOUNDARIES_INVOKER.invoke(histogramBuilder, bucketBoundaries);
        }
        return new OTelDoubleHistogram(BUILD_INVOKER.invoke(histogramBuilder));
    }

    @Override
    public void record(double value, InstrumentationAttributes attributes, InstrumentationContext context) {
        Objects.requireNonNull(attributes, "'attributes' cannot be null.");
        if (isEnabled() && attributes instanceof OTelAttributes) {
            RECORD_INVOKER.invoke(otelHistogram, value, ((OTelAttributes) attributes).getOTelAttributes(),
                OTelContext.fromInstrumentationContext(context));
        }
    }

    @Override
    public boolean isEnabled() {
        return otelHistogram != null && OTelInitializer.isInitialized();
    }
}
