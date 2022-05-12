// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AttributesBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.LongHistogram;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongHistogram implements LongHistogram {
    static final LongHistogram NOOP = new LongHistogram() {
        @Override
        public void record(long value, AttributesBuilder attributes, Context context) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private final io.opentelemetry.api.metrics.LongHistogram histogram;
    OpenTelemetryLongHistogram(io.opentelemetry.api.metrics.LongHistogram histogram) {
        this.histogram = histogram;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void record(long value, AttributesBuilder attributeCollection, Context context) {
        histogram.record(value, Utils.getAttributes(attributeCollection), Utils.getTraceContextOrCurrent(context));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
