// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.LongGauge;
import com.azure.core.util.metrics.Meter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;

import java.util.Map;
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
            otelProvider = otelOptions.getOpenTelemetryProvider();
        }

        this.isEnabled = (options == null || options.isEnabled()) && otelProvider != MeterProvider.noop();
        this.meter = otelProvider.meterBuilder(libraryName).setInstrumentationVersion(libraryVersion).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleHistogram createDoubleHistogram(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (!isEnabled) {
            // we might have per-instrument control later.
            return OpenTelemetryDoubleHistogram.NOOP;
        }

        DoubleHistogramBuilder otelMetricBuilder = meter.histogramBuilder(name).setDescription(description);
        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryDoubleHistogram(otelMetricBuilder.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (!isEnabled) {
            // we might have per-instrument control later.
            return NOOP_COUNTER;
        }

        LongCounterBuilder otelMetricBuilder = meter.counterBuilder(name).setDescription(description);

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

        if (!isEnabled) {
            // we might have per-instrument control later.
            return NOOP_COUNTER;
        }

        LongUpDownCounterBuilder otelMetricBuilder = meter.upDownCounterBuilder(name).setDescription(description);

        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryLongUpDownCounter(otelMetricBuilder.build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongGauge createLongGauge(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (!isEnabled) {
            // we might have per-instrument control later.
            return OpenTelemetryLongGauge.NOOP;
        }

        LongGaugeBuilder otelMetricBuilder = meter.gaugeBuilder(name).setDescription(description).ofLongs();

        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryLongGauge(otelMetricBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TelemetryAttributes createAttributes(Map<String, Object> attributeMap) {
        return new OpenTelemetryAttributes(attributeMap);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void close() {
    }

    private static final LongCounter NOOP_COUNTER = new LongCounter() {
        @Override
        public void add(long value, TelemetryAttributes attributes, Context context) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };
}
