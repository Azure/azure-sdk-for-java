// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AzureAttributeBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.AzureLongHistogram;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongHistogram implements AzureLongHistogram {
    private static final ClientLogger LOGGER = new ClientLogger(OpenTelemetryLongHistogram.class);
    private final LongHistogram histogram;
    OpenTelemetryLongHistogram(LongHistogram histogram) {
        this.histogram = histogram;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void record(long value, AzureAttributeBuilder attributeCollection, Context context) {

        Attributes attributes = Attributes.empty();
        if (attributeCollection instanceof OpenTelemetryAzureAttributeBuilder) {
            attributes = ((OpenTelemetryAzureAttributeBuilder) attributeCollection).build();
        } else if (attributeCollection != null) {
            LOGGER.warning("Expected instance of `OpenTelemetryAttributeBuilder` in `attributeCollection`, but got {}, ignoring it.", attributeCollection.getClass());
        }

        histogram.record(value, attributes, Utils.getTraceContextOrCurrent(context));
    }
}
