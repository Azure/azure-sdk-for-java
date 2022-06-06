// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AttributeBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.AzureLongHistogram;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongHistogram implements AzureLongHistogram {

    private final LongHistogram histogram;
    OpenTelemetryLongHistogram(LongHistogram histogram) {
        this.histogram = histogram;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void record(long value, AttributeBuilder attributeCollection, Context context) {

        Attributes attributes = Attributes.empty();
        if (attributeCollection instanceof OpenTelemetryAttributeBuilder) {
            attributes = ((OpenTelemetryAttributeBuilder) attributeCollection).getAttributes();
        }

        histogram.record(value, attributes, Utils.getTraceContextOrCurrent(context));
    }
}
