// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.metrics;

import io.clientcore.core.util.Context;
import com.azure.core.v2.util.TelemetryAttributes;

import java.util.Map;
import java.util.Objects;

/**
 * {@inheritDoc}
 */
final class NoopMeter implements Meter {
    public static final Meter INSTANCE = new NoopMeter();
    private static final DoubleHistogram NOOP_LONG_HISTOGRAM = new DoubleHistogram() {
        @Override
        public void record(double value, TelemetryAttributes attributes, Context context) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private static final LongCounter NOOP_LONG_COUNTER = new LongCounter() {
        @Override
        public void add(long value, TelemetryAttributes attributes, Context context) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private static final TelemetryAttributes NOOP_ATTRIBUTES = new TelemetryAttributes() {
    };

    private NoopMeter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleHistogram createDoubleHistogram(String name, String description, String unit) {
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
    public TelemetryAttributes createAttributes(Map<String, Object> attributeMap) {
        Objects.requireNonNull(attributeMap, "'attributeMap' cannot be null.");
        for (Map.Entry<String, Object> kvp : attributeMap.entrySet()) {
            Objects.requireNonNull(kvp.getKey(), "'key' cannot be null.");
            Objects.requireNonNull(kvp.getValue(), "'value' cannot be null.");
        }

        return NOOP_ATTRIBUTES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void close() {
    }
}
