// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import java.util.Map;
import java.util.Objects;

/**
 * {@inheritDoc}
 */
final class NoopMeter extends AzureMeter {

    public static final AzureMeter INSTANCE = new NoopMeter();

    private static final AzureLongHistogram NOOP_LONG_HISTOGRAM = (value, context) -> {
    };

    private static final AzureLongCounter NOOP_LONG_COUNTER = (value, context) -> {
    };

    private NoopMeter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureLongHistogram createLongHistogram(String name, String description, String unit, Map<String, Object> attributes) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        return NOOP_LONG_HISTOGRAM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureLongCounter createLongCounter(String name, String description, String unit, Map<String, Object> attributes) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(description, "'description' cannot be null.");
        return NOOP_LONG_COUNTER;
    }
}
