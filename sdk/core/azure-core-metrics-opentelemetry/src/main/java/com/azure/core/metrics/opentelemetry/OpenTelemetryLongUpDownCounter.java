// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongUpDownCounter implements LongCounter {
    private final LongUpDownCounter counter;

    OpenTelemetryLongUpDownCounter(LongUpDownCounter counter) {
        this.counter = counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long value, TelemetryAttributes attributeCollection, Context context) {
        counter.add(value, OpenTelemetryUtils.getAttributes(attributeCollection),
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
