// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AzureAttributeCollection;

import java.util.Objects;

/**
 * {@inheritDoc}
 */
final class NoopMeter implements AzureMeter {

    public static final AzureMeter INSTANCE = new NoopMeter();
    private static final AzureLongHistogram NOOP_LONG_HISTOGRAM = (value, attributes, context) -> {
    };

    private static final AzureLongCounter NOOP_LONG_COUNTER = (value, attributes, context) -> {
    };

    private static final AzureAttributeCollection NOOP_ATTRIBUTES = new AzureAttributeCollection() {
        @Override
        public AzureAttributeCollection add(String key, String value) {
            return this;
        }

        @Override
        public AzureAttributeCollection add(String key, long value) {
            return this;
        }

        @Override
        public AzureAttributeCollection add(String key, double value) {
            return this;
        }

        @Override
        public AzureAttributeCollection add(String key, boolean value) {
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
    public AzureLongCounter createLongUpDownCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        return NOOP_LONG_COUNTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeCollection createAttributeBuilder() {
        return NOOP_ATTRIBUTES;
    }
}
