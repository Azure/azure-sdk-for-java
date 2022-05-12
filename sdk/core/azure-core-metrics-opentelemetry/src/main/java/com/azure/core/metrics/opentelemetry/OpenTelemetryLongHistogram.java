// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.AzureLongHistogram;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongHistogram;

import java.util.Map;
import java.util.Objects;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongHistogram implements AzureLongHistogram {

    private final LongHistogram histogram;
    private final Attributes commonAttributes;
    OpenTelemetryLongHistogram(LongHistogram histogram, Map<String, Object> attributes) {
        this.histogram = histogram;
        if (attributes == null || attributes.isEmpty()) {
            this.commonAttributes = Attributes.empty();
        } else {
            AttributesBuilder attributesBuilder = Attributes.builder();
            attributes.forEach((key, value) -> Utils.addAttribute(attributesBuilder, key, value));
            this.commonAttributes = attributesBuilder.build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void record(long value, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null.");
        histogram.record(value, this.commonAttributes, Utils.getTraceContextOrCurrent(context));
    }
}
