// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AttributesBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.LongCounter;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongCounter implements LongCounter {
    static final LongCounter NOOP = new LongCounter() {
        @Override
        public void add(long value, AttributesBuilder attributes, Context context) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private final io.opentelemetry.api.metrics.LongCounter counter;

    OpenTelemetryLongCounter(io.opentelemetry.api.metrics.LongCounter counter) {
        this.counter = counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long value, AttributesBuilder attributeCollection, Context context) {
        counter.add(value, Utils.getAttributes(attributeCollection), Utils.getTraceContextOrCurrent(context));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
