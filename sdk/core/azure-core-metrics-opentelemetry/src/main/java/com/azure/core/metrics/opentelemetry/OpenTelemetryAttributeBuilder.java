package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AttributeBuilder;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

/**
 * OpenTelemetry-specific implementation of {@link AttributeBuilder}
 */
class OpenTelemetryAttributeBuilder implements AttributeBuilder<Attributes> {
    private final AttributesBuilder builder;
    private Attributes attributes;
    public OpenTelemetryAttributeBuilder() {
        builder = Attributes.builder();
        attributes = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeBuilder addAttribute(String key, String value) {
        builder.put(key, value);
        attributes = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeBuilder addAttribute(String key, long value) {
        builder.put(key, value);
        attributes = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeBuilder addAttribute(String key, double value) {
        builder.put(key, value);
        attributes = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeBuilder addAttribute(String key, boolean value) {
        builder.put(key, value);
        attributes = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attributes getAttributes() {
        if (attributes == null) {
            attributes = builder.build();
        }

        return attributes;
    }
}
