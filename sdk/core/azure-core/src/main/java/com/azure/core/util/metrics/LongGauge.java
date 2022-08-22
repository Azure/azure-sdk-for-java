// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.TelemetryAttributes;

import java.util.function.Supplier;

/**
 * A guauge instrument that records {@code long} values.
 *
 * <p>Gauges record asynchronous measurements that can't be aggregated across different time series, e.g. CPU usage
 * or ServiceBus received sequence number.
 */
public interface LongGauge {
    /**
     * Registers callback to obtain value.
     *
     * @param measurementSupplier Measurement supplier.
     * @param attributes Attributes collection. Gauges only have static attributes.
     * @return Closeable to dispose to stop measurements.
     */
    AutoCloseable setCallback(Supplier<Long> measurementSupplier, TelemetryAttributes attributes);

    /**
     * Flag indicating if metric implementation is detected and functional, use it to minimize performance impact associated with metrics.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    boolean isEnabled();
}
