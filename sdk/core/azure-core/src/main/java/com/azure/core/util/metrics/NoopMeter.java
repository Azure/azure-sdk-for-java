// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AttributesBuilder;
import com.azure.core.util.Context;

import java.util.Objects;

/**
 * {@inheritDoc}
 */
final class NoopMeter implements Meter {
    public static final Meter INSTANCE = new NoopMeter();
    private static final LongHistogram NOOP_LONG_HISTOGRAM = new LongHistogram() {
        @Override
        public void record(long value, AttributesBuilder attributes, Context context) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private static final LongCounter NOOP_LONG_COUNTER = new LongCounter() {
        @Override
        public void add(long value, AttributesBuilder attributes, Context context) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private static final AttributesBuilder NOOP_ATTRIBUTES = new AttributesBuilder() {
        @Override
        public AttributesBuilder add(String key, String value) {
            return this;
        }

        @Override
        public AttributesBuilder add(String key, long value) {
            return this;
        }

        @Override
        public AttributesBuilder add(String key, double value) {
            return this;
        }

        @Override
        public AttributesBuilder add(String key, boolean value) {
            return this;
        }
    };

    private NoopMeter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongHistogram createLongHistogram(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        return NOOP_LONG_HISTOGRAM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        return NOOP_LONG_COUNTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongUpDownCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        return NOOP_LONG_COUNTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder createAttributesBuilder() {
        return NOOP_ATTRIBUTES;
    }

    @Override
    public void close() {
    }
}
