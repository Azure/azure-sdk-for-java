// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.metrics;

import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelAttributes;
import io.clientcore.core.implementation.instrumentation.otel.OTelContext;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.context.Context;

import java.util.List;
import java.util.Objects;

final class OTelDoubleHistogram implements DoubleHistogram {
    private static final DoubleHistogram NOOP = new OTelDoubleHistogram(null);
    private static final ClientLogger LOGGER = new ClientLogger(OTelDoubleHistogram.class);

    private final Object otelHistogram;

    private OTelDoubleHistogram(Object otelHistogram) {
        this.otelHistogram = otelHistogram;
    }

    public static DoubleHistogram create(Object otelMeter, String name, String description, String unit,
        List<Double> bucketBoundaries) {
        if (otelMeter == null || !OTelInitializer.isInitialized()) {
            return NOOP;
        }

        DoubleHistogramBuilder histogramBuilder
            = FallbackInvoker.call(LOGGER, null, () -> ((Meter) otelMeter).histogramBuilder(name));
        FallbackInvoker.run(LOGGER, () -> histogramBuilder.setDescription(description));
        FallbackInvoker.run(LOGGER, () -> histogramBuilder.setUnit(unit));
        if (bucketBoundaries != null) {
            FallbackInvoker.run(LOGGER, () -> histogramBuilder.setExplicitBucketBoundariesAdvice(bucketBoundaries));
        }
        return new OTelDoubleHistogram(FallbackInvoker.call(LOGGER, null, () -> histogramBuilder.build()));
    }

    @Override
    public void record(double value, InstrumentationAttributes attributes, InstrumentationContext context) {
        Objects.requireNonNull(attributes, "'attributes' cannot be null.");
        if (isEnabled() && attributes instanceof OTelAttributes) {
            FallbackInvoker.run(LOGGER,
                () -> ((io.opentelemetry.api.metrics.DoubleHistogram) otelHistogram).record(value,
                    (Attributes) ((OTelAttributes) attributes).getOTelAttributes(),
                    (Context) OTelContext.fromInstrumentationContext(context)));
        }
    }

    @Override
    public boolean isEnabled() {
        return otelHistogram != null && OTelInitializer.isInitialized();
    }
}
