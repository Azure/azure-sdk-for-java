// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.metrics;

import io.clientcore.core.util.Context;
import com.azure.core.v2.util.TelemetryAttributes;

/**
 * A counter instrument that records {@code long} values.
 *
 * <p>
 * Counters only allow adding positive values, and guarantee the resulting metrics will be
 * always-increasing monotonic sums.
 */
public interface LongCounter {
    /**
     * Records a value with a set of attributes.
     *
     * @param value The amount of the measurement.
     * @param attributes Collection of attributes representing metric dimensions.
     * @param context The explicit context to associate with this measurement.
     */
    void add(long value, TelemetryAttributes attributes, Context context);

    /**
     * Flag indicating if metric implementation is detected and functional, use it to minimize performance impact associated with metrics,
     * e.g. measuring latency.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    boolean isEnabled();
}
