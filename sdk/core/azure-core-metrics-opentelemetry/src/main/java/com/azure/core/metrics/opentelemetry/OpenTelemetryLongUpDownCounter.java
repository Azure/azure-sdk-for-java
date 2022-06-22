// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AzureAttributeCollection;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.AzureLongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongUpDownCounter implements AzureLongCounter {
    private final LongUpDownCounter counter;

    OpenTelemetryLongUpDownCounter(LongUpDownCounter counter) {
        this.counter = counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long value, AzureAttributeCollection attributeCollection, Context context) {
        counter.add(value, Utils.getAttributes(attributeCollection), Utils.getTraceContextOrCurrent(context));
    }
}
