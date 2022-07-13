// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongUpDownCounter implements LongCounter {
    static final LongHistogram NOOP = new LongHistogram() {
        @Override
        public void record(long value, TelemetryAttributes attributes, Context context) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private final LongUpDownCounter counter;

    OpenTelemetryLongUpDownCounter(LongUpDownCounter counter) {
        this.counter = counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long value, TelemetryAttributes attributeCollection, Context context) {
        counter.add(value, OpenTelemetryUtils.getAttributes(attributeCollection), OpenTelemetryUtils.getTraceContextOrCurrent(context));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
