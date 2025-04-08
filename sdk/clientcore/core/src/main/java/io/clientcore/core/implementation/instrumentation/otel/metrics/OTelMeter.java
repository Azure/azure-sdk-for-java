// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.metrics;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.NoopMeter;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.LongCounter;
import io.clientcore.core.instrumentation.metrics.Meter;

import java.util.List;
import java.util.Objects;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.METER_BUILDER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.METER_PROVIDER_CLASS;

/**
 * A class that wraps the OpenTelemetry meter.
 */
public class OTelMeter implements Meter {
    private static final ClientLogger LOGGER = new ClientLogger(OTelMeter.class);
    private static final FallbackInvoker SET_INSTRUMENTATION_VERSION_INVOKER;
    private static final FallbackInvoker BUILD_INVOKER;
    private static final FallbackInvoker SET_SCHEMA_URL_INVOKER;
    private static final FallbackInvoker GET_METER_BUILDER_INVOKER;

    static {
        ReflectiveInvoker setInstrumentationVersionInvoker = null;
        ReflectiveInvoker buildInvoker = null;
        ReflectiveInvoker setSchemaUrlInvoker = null;
        ReflectiveInvoker getMeterBuilderInvoker = null;

        if (OTelInitializer.isInitialized()) {
            try {
                setInstrumentationVersionInvoker = getMethodInvoker(METER_BUILDER_CLASS,
                    METER_BUILDER_CLASS.getMethod("setInstrumentationVersion", String.class));

                setSchemaUrlInvoker = getMethodInvoker(METER_BUILDER_CLASS,
                    METER_BUILDER_CLASS.getMethod("setSchemaUrl", String.class));

                buildInvoker = getMethodInvoker(METER_BUILDER_CLASS, METER_BUILDER_CLASS.getMethod("build"));

                getMeterBuilderInvoker = getMethodInvoker(METER_PROVIDER_CLASS,
                    METER_PROVIDER_CLASS.getMethod("meterBuilder", String.class));
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        SET_INSTRUMENTATION_VERSION_INVOKER = new FallbackInvoker(setInstrumentationVersionInvoker, LOGGER);
        SET_SCHEMA_URL_INVOKER = new FallbackInvoker(setSchemaUrlInvoker, LOGGER);
        BUILD_INVOKER = new FallbackInvoker(buildInvoker, NoopMeter.INSTANCE, LOGGER);
        GET_METER_BUILDER_INVOKER = new FallbackInvoker(getMeterBuilderInvoker, LOGGER);
    }

    private final Object otelMeter;

    /**
     * Creates a new instance of OTelMeter.
     *
     * @param otelMeterProvider The OpenTelemetry meter provider.
     * @param sdkOptions The library options.
     */
    public OTelMeter(Object otelMeterProvider, SdkInstrumentationOptions sdkOptions) {
        Object meterBuilder = GET_METER_BUILDER_INVOKER.invoke(otelMeterProvider, sdkOptions.getSdkName());
        if (meterBuilder != null) {
            SET_INSTRUMENTATION_VERSION_INVOKER.invoke(meterBuilder, sdkOptions.getSdkVersion());
            SET_SCHEMA_URL_INVOKER.invoke(meterBuilder, sdkOptions.getSchemaUrl());
            this.otelMeter = BUILD_INVOKER.invoke(meterBuilder);
        } else {
            this.otelMeter = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleHistogram createDoubleHistogram(String name, String description, String unit,
        List<Double> bucketBoundaries) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        Objects.requireNonNull(unit, "'unit' cannot be null.");
        return OTelDoubleHistogram.create(otelMeter, name, description, unit, bucketBoundaries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        Objects.requireNonNull(unit, "'unit' cannot be null.");
        return OTelLongCounter.create(otelMeter, name, description, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongUpDownCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        Objects.requireNonNull(unit, "'unit' cannot be null.");
        return OTelLongUpDownCounter.create(otelMeter, name, description, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return otelMeter != null && OTelInitializer.isInitialized();
    }
}
