// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AttributesBuilder;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.LongHistogram;
import com.azure.core.util.metrics.Meter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;

import java.util.Objects;

/**
 * {@inheritDoc}
 */
class OpenTelemetryMeter implements Meter {
    private final io.opentelemetry.api.metrics.Meter meter;
    private final boolean isEnabled;

    OpenTelemetryMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        MeterProvider otelProvider = GlobalOpenTelemetry.getMeterProvider();
        if (options != null && options.isEnabled() && options instanceof OpenTelemetryMetricsOptions) {
            OpenTelemetryMetricsOptions otelOptions = (OpenTelemetryMetricsOptions) options;
            otelProvider = otelOptions.getProvider();
        }

        this.isEnabled = (options == null || options.isEnabled()) && otelProvider != MeterProvider.noop();
        this.meter = otelProvider.meterBuilder(libraryName)
            .setInstrumentationVersion(libraryVersion)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongHistogram createLongHistogram(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (isEnabled) {
            // we might have per-instrument control later.
            return OpenTelemetryLongHistogram.NOOP;
        }

        LongHistogramBuilder otelMetricBuilder = meter.histogramBuilder(name)
            .setDescription(description)
            .ofLongs();
        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryLongHistogram(otelMetricBuilder.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (isEnabled) {
            // we might have per-instrument control later.
            return OpenTelemetryLongCounter.NOOP;
        }

        LongCounterBuilder otelMetricBuilder = meter.counterBuilder(name)
            .setDescription(description);

        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryLongCounter(otelMetricBuilder.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongUpDownCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");

        LongUpDownCounterBuilder otelMetricBuilder = meter.upDownCounterBuilder(name)
            .setDescription(description);

        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryLongUpDownCounter(otelMetricBuilder.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder createAttributesBuilder() {
        return new OpenTelemetryAttributesBuilder();
    }

    @Override
    public void close() {
    }
}
