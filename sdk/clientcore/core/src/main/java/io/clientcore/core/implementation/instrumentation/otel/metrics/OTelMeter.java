// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.metrics;

import io.clientcore.core.implementation.instrumentation.NoopMeter;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.LongCounter;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;

import java.util.List;
import java.util.Objects;

/**
 * A class that wraps the OpenTelemetry meter.
 */
public class OTelMeter implements Meter {
    private static final ClientLogger LOGGER = new ClientLogger(OTelMeter.class);

    private final Object otelMeter;

    /**
     * Creates a new instance of OTelMeter.
     *
     * @param otelMeterProvider The OpenTelemetry meter provider.
     * @param sdkOptions The library options.
     */
    public OTelMeter(Object otelMeterProvider, SdkInstrumentationOptions sdkOptions) {
        MeterBuilder meterBuilder = FallbackInvoker.call(LOGGER, null,
            () -> ((MeterProvider) otelMeterProvider).meterBuilder(sdkOptions.getSdkName()));
        if (meterBuilder != null) {
            FallbackInvoker.run(LOGGER, () -> meterBuilder.setInstrumentationVersion(sdkOptions.getSdkVersion()));
            FallbackInvoker.run(LOGGER, () -> meterBuilder.setSchemaUrl(sdkOptions.getSchemaUrl()));
            this.otelMeter = FallbackInvoker.call(LOGGER, NoopMeter.INSTANCE, () -> meterBuilder.build());
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
