// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.LongGauge;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongGaugeBuilder;

import java.util.function.Supplier;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongGauge implements LongGauge {
    private final LongGaugeBuilder longGaugeBuilder;

    OpenTelemetryLongGauge(LongGaugeBuilder longGaugeBuilder) {
        this.longGaugeBuilder = longGaugeBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AutoCloseable setCallback(Supplier<Long> measurementSupplier, TelemetryAttributes attributes) {
        final Attributes otelAttributes =  (attributes instanceof OpenTelemetryAttributes)
            ? ((OpenTelemetryAttributes) attributes).get() : Attributes.empty();
        // TODO (limolkova): warning if not otel attributes

        return longGaugeBuilder.buildWithCallback(obs -> obs.record(measurementSupplier.get(), otelAttributes));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
