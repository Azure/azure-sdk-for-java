package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AzureAttributeCollection;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.Objects;

/**
 * OpenTelemetry-specific implementation of {@link AzureAttributeCollection}
 */
class OpenTelemetryAzureAttributeCollection implements AzureAttributeCollection {
    private final AttributesBuilder builder;
    private Attributes attributes;
    public OpenTelemetryAzureAttributeCollection() {
        builder = Attributes.builder();
        attributes = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeCollection add(String key, String value) {
        Objects.requireNonNull(key, "'key' cannot be null");
        Objects.requireNonNull(value, "'value' cannot be null");
        builder.put(key, value);
        attributes = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeCollection add(String key, long value) {
        Objects.requireNonNull(key, "'key' cannot be null");
        builder.put(key, value);
        attributes = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeCollection add(String key, double value) {
        Objects.requireNonNull(key, "'key' cannot be null");
        builder.put(key, value);
        attributes = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeCollection add(String key, boolean value) {
        Objects.requireNonNull(key, "'key' cannot be null");
        builder.put(key, value);
        attributes = null;
        return this;
    }

    Attributes build() {
        if (attributes == null) {
            attributes = builder.build();
        }

        return attributes;
    }
}
