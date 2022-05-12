// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.AzureLongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;

import java.util.Map;
import java.util.Objects;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongCounter implements AzureLongCounter {
    private final LongCounter counter;
    private final Attributes commonAttributes;

    OpenTelemetryLongCounter(LongCounter counter, Map<String, Object> attributes) {
        this.counter = counter;
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
    public void add(long value, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null.");
        counter.add(value, this.commonAttributes, Utils.getTraceContextOrCurrent(context));
    }
}
