package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.AzureLongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;

import java.util.Map;

class OpenTelemetryLongCounter implements AzureLongCounter {

    private final LongCounter counter;
    private final Attributes commonAttributes;
    OpenTelemetryLongCounter(LongCounter counter, Map<String, Object> attributes) {
        this.counter = counter;
        if (attributes == null || attributes.isEmpty()) {
            this.commonAttributes = Attributes.empty();
        } else {
            AttributesBuilder attributesBuilder = Attributes.builder();
            attributes.forEach((key, value) -> AttributeHelper.convertToOtelAttribute(attributesBuilder, key, value));
            this.commonAttributes = attributesBuilder.build();
        }
    }

    @Override
    public void add(long value, Context context) {
        counter.add(value, this.commonAttributes, AttributeHelper.getTraceContextOrDefault(context));
    }
}
