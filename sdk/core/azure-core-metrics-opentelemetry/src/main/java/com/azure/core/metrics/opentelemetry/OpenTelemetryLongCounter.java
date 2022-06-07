// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AzureAttributeBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.AzureLongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongCounter implements AzureLongCounter {
    private static final ClientLogger LOGGER = new ClientLogger(OpenTelemetryLongCounter.class);
    private final LongCounter counter;

    OpenTelemetryLongCounter(LongCounter counter) {
        this.counter = counter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(long value, AzureAttributeBuilder attributeCollection, Context context) {
        Attributes attributes = Attributes.empty();
        if (attributeCollection instanceof OpenTelemetryAzureAttributeBuilder) {
            attributes = ((OpenTelemetryAzureAttributeBuilder) attributeCollection).build();
        } else if (attributeCollection != null) {
            LOGGER.warning("Expected instance of `OpenTelemetryAttributeBuilder` in `attributeCollection`, but got {}, ignoring it.", attributeCollection.getClass());
        }

        counter.add(value, attributes, Utils.getTraceContextOrCurrent(context));
    }
}
