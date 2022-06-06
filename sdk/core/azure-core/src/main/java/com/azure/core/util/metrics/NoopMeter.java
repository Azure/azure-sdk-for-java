// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AttributeBuilder;

import java.util.Objects;

/**
 * {@inheritDoc}
 */
final class NoopMeter extends AzureMeter {

    public static final AzureMeter INSTANCE = new NoopMeter();

    private static final AzureLongHistogram NOOP_LONG_HISTOGRAM = (value, attributes, context) -> {
    };

    private static final AzureLongCounter NOOP_LONG_COUNTER = (value, attributes, context) -> {
    };

    private static final AttributeBuilder NOOP_ATTRIBUTES = new AttributeBuilder() {
        @Override
        public AttributeBuilder addAttribute(String key, String value) {
            return this;
        }

        @Override
        public AttributeBuilder addAttribute(String key, long value) {
            return this;
        }

        @Override
        public AttributeBuilder addAttribute(String key, double value) {
            return this;
        }

        @Override
        public AttributeBuilder addAttribute(String key, boolean value) {
            return this;
        }
    };

    private NoopMeter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureLongHistogram createLongHistogram(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        return NOOP_LONG_HISTOGRAM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureLongCounter createLongCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        return NOOP_LONG_COUNTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeBuilder createAttributesBuilder() {
        return NOOP_ATTRIBUTES;
    }
}
