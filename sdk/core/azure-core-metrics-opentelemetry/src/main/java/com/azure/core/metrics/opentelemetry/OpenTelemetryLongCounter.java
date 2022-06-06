// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AttributeBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.AzureLongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongCounter implements AzureLongCounter {
    private final LongCounter counter;

    OpenTelemetryLongCounter(LongCounter counter) {
        this.counter = counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long value, AttributeBuilder attributeCollection, Context context) {
        Attributes attributes = Attributes.empty();
        if (attributeCollection instanceof OpenTelemetryAttributeBuilder) {
            attributes = ((OpenTelemetryAttributeBuilder) attributeCollection).getAttributes();
        }

        counter.add(value, attributes, Utils.getTraceContextOrCurrent(context));
    }
}
