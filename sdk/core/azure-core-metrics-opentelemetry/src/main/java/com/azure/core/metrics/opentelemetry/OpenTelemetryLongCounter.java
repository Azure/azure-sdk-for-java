// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.LongCounter;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongCounter implements LongCounter {
    private final io.opentelemetry.api.metrics.LongCounter counter;

    OpenTelemetryLongCounter(io.opentelemetry.api.metrics.LongCounter counter) {
        this.counter = counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long value, TelemetryAttributes attributes, Context context) {
        counter.add(value, OpenTelemetryUtils.getAttributes(attributes),
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
