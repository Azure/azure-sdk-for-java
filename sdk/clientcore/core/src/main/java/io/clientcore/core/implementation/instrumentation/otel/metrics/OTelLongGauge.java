// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.metrics;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelAttributes;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.metrics.LongGauge;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTES_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.DOUBLE_GAUGE_BUILDER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.LONG_GAUGE_BUILDER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.METER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.OBSERVABLE_LONG_GAUGE_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.OBSERVABLE_LONG_MEASUREMENT_CLASS;

final class OTelLongGauge implements LongGauge {
    private static final ClientLogger LOGGER = new ClientLogger(OTelLongGauge.class);
    private static final AutoCloseable NOOP_REGISTRATION = () -> {
    };
    private static final FallbackInvoker GAUGE_BUILDER_INVOKER;
    private static final FallbackInvoker OF_LONGS_INVOKER;
    private static final FallbackInvoker SET_DESCRIPTION_INVOKER;
    private static final FallbackInvoker SET_UNIT_INVOKER;
    private static final FallbackInvoker BUILD_WITH_CALLBACK_INVOKER;
    private static final FallbackInvoker RECORD_INVOKER;
    private static final FallbackInvoker CLOSE_INVOKER;

    static {
        ReflectiveInvoker gaugeBuilderInvoker = null;
        ReflectiveInvoker ofLongsInvoker = null;
        ReflectiveInvoker setDescriptionInvoker = null;
        ReflectiveInvoker setUnitInvoker = null;
        ReflectiveInvoker buildWithCallbackInvoker = null;
        ReflectiveInvoker recordInvoker = null;
        ReflectiveInvoker closeInvoker = null;

        if (OTelInitializer.isInitialized()) {
            try {
                gaugeBuilderInvoker
                    = getMethodInvoker(METER_CLASS, METER_CLASS.getMethod("gaugeBuilder", String.class));
                ofLongsInvoker
                    = getMethodInvoker(DOUBLE_GAUGE_BUILDER_CLASS, DOUBLE_GAUGE_BUILDER_CLASS.getMethod("ofLongs"));
                setDescriptionInvoker = getMethodInvoker(LONG_GAUGE_BUILDER_CLASS,
                    LONG_GAUGE_BUILDER_CLASS.getMethod("setDescription", String.class));
                setUnitInvoker = getMethodInvoker(LONG_GAUGE_BUILDER_CLASS,
                    LONG_GAUGE_BUILDER_CLASS.getMethod("setUnit", String.class));
                buildWithCallbackInvoker = getMethodInvoker(LONG_GAUGE_BUILDER_CLASS,
                    LONG_GAUGE_BUILDER_CLASS.getMethod("buildWithCallback", Consumer.class));
                recordInvoker = getMethodInvoker(OBSERVABLE_LONG_MEASUREMENT_CLASS,
                    OBSERVABLE_LONG_MEASUREMENT_CLASS.getMethod("record", long.class, ATTRIBUTES_CLASS));
                closeInvoker
                    = getMethodInvoker(OBSERVABLE_LONG_GAUGE_CLASS, OBSERVABLE_LONG_GAUGE_CLASS.getMethod("close"));
            } catch (Throwable throwable) {
                OTelInitializer.initError(LOGGER, throwable);
            }
        }

        GAUGE_BUILDER_INVOKER = new FallbackInvoker(gaugeBuilderInvoker, LOGGER);
        OF_LONGS_INVOKER = new FallbackInvoker(ofLongsInvoker, LOGGER);
        SET_DESCRIPTION_INVOKER = new FallbackInvoker(setDescriptionInvoker, LOGGER);
        SET_UNIT_INVOKER = new FallbackInvoker(setUnitInvoker, LOGGER);
        BUILD_WITH_CALLBACK_INVOKER = new FallbackInvoker(buildWithCallbackInvoker, LOGGER);
        RECORD_INVOKER = new FallbackInvoker(recordInvoker, LOGGER);
        CLOSE_INVOKER = new FallbackInvoker(closeInvoker, LOGGER);
    }

    private final Object gaugeBuilder;

    private OTelLongGauge(Object gaugeBuilder) {
        this.gaugeBuilder = gaugeBuilder;
    }

    static LongGauge create(Object otelMeter, String name, String description, String unit) {
        if (otelMeter == null || !OTelInitializer.isInitialized()) {
            return new OTelLongGauge(null);
        }

        Object doubleGaugeBuilder = GAUGE_BUILDER_INVOKER.invoke(otelMeter, name);
        Object longGaugeBuilder = OF_LONGS_INVOKER.invoke(doubleGaugeBuilder);
        SET_DESCRIPTION_INVOKER.invoke(longGaugeBuilder, description);
        if (unit != null) {
            SET_UNIT_INVOKER.invoke(longGaugeBuilder, unit);
        }
        return new OTelLongGauge(longGaugeBuilder);
    }

    @Override
    public AutoCloseable registerCallback(Supplier<Long> valueSupplier, InstrumentationAttributes attributes) {
        Objects.requireNonNull(valueSupplier, "'valueSupplier' cannot be null.");
        Objects.requireNonNull(attributes, "'attributes' cannot be null.");
        if (!isEnabled() || !(attributes instanceof OTelAttributes)) {
            return NOOP_REGISTRATION;
        }

        Object otelAttributes = ((OTelAttributes) attributes).getOTelAttributes();
        Consumer<Object> callback = measurement -> {
            try {
                Long value = valueSupplier.get();
                if (value != null) {
                    RECORD_INVOKER.invoke(measurement, value, otelAttributes);
                }
            } catch (RuntimeException exception) {
                LOGGER.atWarning().setThrowable(exception).log("Long gauge callback failed.");
            }
        };
        Object registration = BUILD_WITH_CALLBACK_INVOKER.invoke(gaugeBuilder, callback);
        return () -> CLOSE_INVOKER.invoke(registration);
    }

    @Override
    public boolean isEnabled() {
        return gaugeBuilder != null && OTelInitializer.isInitialized();
    }
}
