// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.LongGauge;
import io.opentelemetry.api.metrics.LongGaugeBuilder;

import java.util.function.Supplier;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongGauge implements LongGauge {
    private static final AutoCloseable NOOP_CLOSEABLE = () -> {
    };

    static final LongGauge NOOP = new LongGauge() {
        @Override
        public AutoCloseable registerCallback(Supplier<Long> valueSupplier, TelemetryAttributes attributes) {
            return NOOP_CLOSEABLE;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private final LongGaugeBuilder gaugeBuilder;

    OpenTelemetryLongGauge(LongGaugeBuilder gaugeBuilder) {
        this.gaugeBuilder = gaugeBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AutoCloseable registerCallback(Supplier<Long> valueSupplier, TelemetryAttributes attributes) {
        return gaugeBuilder.buildWithCallback(
            (measurement) -> measurement.record(valueSupplier.get(), OpenTelemetryUtils.getAttributes(attributes)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
