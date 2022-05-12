// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AttributesBuilder;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

import java.util.Objects;

/**
 * OpenTelemetry-specific implementation of {@link AttributesBuilder}
 */
class OpenTelemetryAttributesBuilder implements AttributesBuilder {
    private final io.opentelemetry.api.common.AttributesBuilder builder;
    private Attributes attributes;
    OpenTelemetryAttributesBuilder() {
        builder = Attributes.builder();
        attributes = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder add(String key, String value) {
        Objects.requireNonNull(key, "'key' cannot be null");
        Objects.requireNonNull(value, "'value' cannot be null");
        builder.put(AttributeKey.stringKey(key), value);
        attributes = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder add(String key, long value) {
        Objects.requireNonNull(AttributeKey.longKey(key), "'key' cannot be null");
        builder.put(key, value);
        attributes = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder add(String key, double value) {
        Objects.requireNonNull(AttributeKey.doubleKey(key), "'key' cannot be null");
        builder.put(key, value);
        attributes = null;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder add(String key, boolean value) {
        Objects.requireNonNull(AttributeKey.booleanKey(key), "'key' cannot be null");
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
