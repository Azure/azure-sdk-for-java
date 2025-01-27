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
import io.clientcore.core.instrumentation.metrics.LongCounter;

import java.util.Objects;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTES_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.LONG_UP_DOWN_COUNTER_BUILDER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.LONG_UP_DOWN_COUNTER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.METER_CLASS;

final class OTelLongUpDownCounter implements LongCounter {
    private static final LongCounter NOOP = new OTelLongUpDownCounter(null);
    private static final ClientLogger LOGGER = new ClientLogger(OTelLongUpDownCounter.class);
    private static final FallbackInvoker ADD_INVOKER;
    private static final FallbackInvoker UP_DOWN_COUNTER_BUILDER_INVOKER;
    private static final FallbackInvoker SET_DESCRIPTION_INVOKER;
    private static final FallbackInvoker SET_UNIT_INVOKER;
    private static final FallbackInvoker BUILD_INVOKER;

    private final Object otelCounter;

    static {
        ReflectiveInvoker addInvoker = null;
        ReflectiveInvoker upDownBuilderInvoker = null;
        ReflectiveInvoker setDescriptionInvoker = null;
        ReflectiveInvoker setUnitInvoker = null;
        ReflectiveInvoker buildInvoker = null;

        if (OTelInitializer.isInitialized()) {
            try {
                upDownBuilderInvoker
                    = getMethodInvoker(METER_CLASS, METER_CLASS.getMethod("upDownCounterBuilder", String.class));

                setDescriptionInvoker = getMethodInvoker(LONG_UP_DOWN_COUNTER_BUILDER_CLASS,
                    LONG_UP_DOWN_COUNTER_BUILDER_CLASS.getMethod("setDescription", String.class));

                setUnitInvoker = getMethodInvoker(LONG_UP_DOWN_COUNTER_BUILDER_CLASS,
                    LONG_UP_DOWN_COUNTER_BUILDER_CLASS.getMethod("setUnit", String.class));

                buildInvoker = getMethodInvoker(LONG_UP_DOWN_COUNTER_BUILDER_CLASS,
                    LONG_UP_DOWN_COUNTER_BUILDER_CLASS.getMethod("build"));

                addInvoker = getMethodInvoker(LONG_UP_DOWN_COUNTER_CLASS,
                    LONG_UP_DOWN_COUNTER_CLASS.getMethod("add", long.class, ATTRIBUTES_CLASS, CONTEXT_CLASS));
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        UP_DOWN_COUNTER_BUILDER_INVOKER = new FallbackInvoker(upDownBuilderInvoker, LOGGER);
        SET_DESCRIPTION_INVOKER = new FallbackInvoker(setDescriptionInvoker, LOGGER);
        SET_UNIT_INVOKER = new FallbackInvoker(setUnitInvoker, LOGGER);
        BUILD_INVOKER = new FallbackInvoker(buildInvoker, LOGGER);
        ADD_INVOKER = new FallbackInvoker(addInvoker, LOGGER);
    }

    private OTelLongUpDownCounter(Object otelCounter) {
        this.otelCounter = otelCounter;
    }

    public static LongCounter create(Object otelMeter, String name, String description, String unit) {
        if (otelMeter == null || !OTelInitializer.isInitialized()) {
            return NOOP;
        }

        Object counterBuilder = UP_DOWN_COUNTER_BUILDER_INVOKER.invoke(otelMeter, name);
        SET_DESCRIPTION_INVOKER.invoke(counterBuilder, description);

        if (!Objects.isNull(unit)) {
            SET_UNIT_INVOKER.invoke(counterBuilder, unit);
        }

        return new OTelLongUpDownCounter(BUILD_INVOKER.invoke(counterBuilder));
    }

    @Override
    public void add(long value, InstrumentationAttributes attributes, InstrumentationContext context) {
        Objects.requireNonNull(attributes, "'attributes' cannot be null.");
        if (isEnabled() && attributes instanceof OTelAttributes) {
            ADD_INVOKER.invoke(otelCounter, value, ((OTelAttributes) attributes).getOTelAttributes(),
                OTelContext.fromInstrumentationContext(context));
        }
    }

    @Override
    public boolean isEnabled() {
        return otelCounter != null && OTelInitializer.isInitialized();
    }
}
