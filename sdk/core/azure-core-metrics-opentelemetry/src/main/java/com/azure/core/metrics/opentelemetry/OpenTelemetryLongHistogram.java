package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.AzureLongHistogram;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongHistogram;

import java.util.Map;

class OpenTelemetryLongHistogram implements AzureLongHistogram {

    private final io.opentelemetry.api.metrics.LongHistogram histogram;
    private final Attributes commonAttributes;
    OpenTelemetryLongHistogram(LongHistogram histogram, Map<String, Object> attributes) {
        this.histogram = histogram;
        if (attributes == null || attributes.isEmpty()) {
            this.commonAttributes = Attributes.empty();
        } else {
            AttributesBuilder attributesBuilder = Attributes.builder();
            attributes.forEach((key, value) -> AttributeHelper.convertToOtelAttribute(attributesBuilder, key, value));
            this.commonAttributes = attributesBuilder.build();
        }
    }

    @Override
    public void record(long value, Context context) {
        histogram.record(value, this.commonAttributes, AttributeHelper.getTraceContextOrDefault(context));
    }
}
