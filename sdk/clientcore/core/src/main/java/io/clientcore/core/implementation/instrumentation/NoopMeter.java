// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.LongCounter;
import io.clientcore.core.instrumentation.metrics.Meter;

import java.util.List;
import java.util.Objects;

/**
 * {@inheritDoc}
 */
public final class NoopMeter implements Meter {
    public static final Meter INSTANCE = new NoopMeter();
    public static final DoubleHistogram NOOP_LONG_HISTOGRAM = new DoubleHistogram() {
        @Override
        public void record(double value, InstrumentationAttributes attributes, InstrumentationContext context) {
            Objects.requireNonNull(attributes, "'attributes' cannot be null.");
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private static final LongCounter NOOP_LONG_COUNTER = new LongCounter() {
        @Override
        public void add(long value, InstrumentationAttributes attributes, InstrumentationContext context) {
            Objects.requireNonNull(attributes, "'attributes' cannot be null.");
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private NoopMeter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleHistogram createDoubleHistogram(String name, String description, String unit,
        List<Double> bucketBoundaries) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        Objects.requireNonNull(unit, "'unit' cannot be null.");
        return NOOP_LONG_HISTOGRAM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        Objects.requireNonNull(unit, "'unit' cannot be null.");
        return NOOP_LONG_COUNTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongUpDownCounter(String name, String description, String unit) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        Objects.requireNonNull(unit, "'unit' cannot be null.");
        return NOOP_LONG_COUNTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return false;
    }
}
