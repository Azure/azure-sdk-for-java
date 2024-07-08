// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.metrics;

import com.azure.core.v2.util.TelemetryAttributes;

import java.util.function.Supplier;

/**
 * A counter instrument that records {@code long} values.
 *
 * <p>
 * Counters only allow adding positive values, and guarantee the resulting metrics will be
 * always-increasing monotonic sums.
 */
public interface LongGauge {
    /**
     * Registers callbacks to obtain measurements. Make sure to close result to stop reporting metric.
     *
     * @param valueSupplier Callback that will periodically be requested to obtain current value.
     * @param attributes Collection of attributes representing metric dimensions. Caller that wants to
     *                   record dynamic attributes, should register callback per each attribute combination.
     * @return instance of {@link AutoCloseable} subscription.
     */
    AutoCloseable registerCallback(Supplier<Long> valueSupplier, TelemetryAttributes attributes);

    /**
     * Flag indicating if metric implementation is detected and functional, use it to minimize performance impact associated with metrics,
     * e.g. measuring latency.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    boolean isEnabled();
}
