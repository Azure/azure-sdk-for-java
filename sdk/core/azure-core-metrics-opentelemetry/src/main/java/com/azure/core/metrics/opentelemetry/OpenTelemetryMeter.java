// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.AzureLongCounter;
import com.azure.core.util.metrics.AzureLongHistogram;
import com.azure.core.util.metrics.AzureMeter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;

import java.util.Map;
import java.util.Objects;

/**
 * {@inheritDoc}
 */
class OpenTelemetryMeter extends AzureMeter {
    private final Meter meter;
    private final boolean isEnabled;

    OpenTelemetryMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        MeterProvider otelProvider = GlobalOpenTelemetry.getMeterProvider();
        if (options != null) {
            if (options.isEnabled()) {
                Object providerObj = options.getProvider();
                if (providerObj != null && MeterProvider.class.isAssignableFrom(providerObj.getClass())) {
                    otelProvider = (MeterProvider) options.getProvider();
                }
            }  else {
                otelProvider = MeterProvider.noop();
            }
        }

        this.isEnabled = otelProvider != MeterProvider.noop();
        this.meter = otelProvider.meterBuilder(libraryName)
            .setInstrumentationVersion(libraryVersion)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureLongHistogram createLongHistogram(String name, String description, String unit, Map<String, Object> attributes) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");

        LongHistogramBuilder otelMetricBuilder = meter.histogramBuilder(name)
            .setDescription(description)
            .ofLongs();
        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryLongHistogram(otelMetricBuilder.build(), attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureLongCounter createLongCounter(String name, String description, String unit, Map<String, Object> attributes) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");

        LongCounterBuilder otelMetricBuilder = meter.counterBuilder(name)
            .setDescription(description);

        if (!CoreUtils.isNullOrEmpty(unit)) {
            otelMetricBuilder.setUnit(unit);
        }

        return new OpenTelemetryLongCounter(otelMetricBuilder.build(), attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }
}
