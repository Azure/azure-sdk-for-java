// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.DoubleHistogram;

/**
 * {@inheritDoc}
 */
class OpenTelemetryDoubleHistogram implements DoubleHistogram {
    static final DoubleHistogram NOOP = new DoubleHistogram() {
        @Override
        public void record(double value, TelemetryAttributes attributes, Context context) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private final io.opentelemetry.api.metrics.DoubleHistogram histogram;

    OpenTelemetryDoubleHistogram(io.opentelemetry.api.metrics.DoubleHistogram histogram) {
        this.histogram = histogram;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void record(double value, TelemetryAttributes attributes, Context context) {
        histogram.record(value, OpenTelemetryUtils.getAttributes(attributes),
            OpenTelemetryUtils.getTraceContextOrCurrent(context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
