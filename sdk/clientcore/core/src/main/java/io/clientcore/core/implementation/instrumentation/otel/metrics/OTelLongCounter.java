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
import io.clientcore.core.instrumentation.metrics.LongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.context.Context;

import java.util.Objects;

final class OTelLongCounter implements LongCounter {
    private static final LongCounter NOOP = new OTelLongCounter(null);
    private static final ClientLogger LOGGER = new ClientLogger(OTelLongCounter.class);

    private final Object otelCounter;

    private OTelLongCounter(Object otelCounter) {
        this.otelCounter = otelCounter;
    }

    public static LongCounter create(Object otelMeter, String name, String description, String unit) {
        if (otelMeter == null || !OTelInitializer.isInitialized()) {
            return NOOP;
        }

        LongCounterBuilder counterBuilder
            = FallbackInvoker.call(LOGGER, null, () -> ((Meter) otelMeter).counterBuilder(name));
        FallbackInvoker.run(LOGGER, () -> counterBuilder.setDescription(description));

        if (!Objects.isNull(unit)) {
            FallbackInvoker.run(LOGGER, () -> counterBuilder.setUnit(unit));
        }

        return new OTelLongCounter(FallbackInvoker.call(LOGGER, null, () -> counterBuilder.build()));
    }

    @Override
    public void add(long value, InstrumentationAttributes attributes, InstrumentationContext context) {
        Objects.requireNonNull(attributes, "'attributes' cannot be null.");
        if (isEnabled() && attributes instanceof OTelAttributes) {
            FallbackInvoker.run(LOGGER,
                () -> ((io.opentelemetry.api.metrics.LongCounter) otelCounter).add(value,
                    (Attributes) ((OTelAttributes) attributes).getOTelAttributes(),
                    (Context) OTelContext.fromInstrumentationContext(context)));
        }
    }

    @Override
    public boolean isEnabled() {
        return otelCounter != null && OTelInitializer.isInitialized();
    }
}
