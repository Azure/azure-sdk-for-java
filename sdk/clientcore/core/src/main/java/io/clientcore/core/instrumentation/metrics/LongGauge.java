// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.metrics;

import io.clientcore.core.instrumentation.InstrumentationAttributes;

import java.util.function.Supplier;

/**
 * An observable instrument that reports current {@code long} values.
 */
public interface LongGauge {
    /**
     * Registers a callback that supplies the current value when metrics are collected.
     *
     * @param valueSupplier Supplies the current value.
     * @param attributes Attributes associated with the measurement.
     * @return A registration that must be closed to stop reporting the measurement.
     */
    AutoCloseable registerCallback(Supplier<Long> valueSupplier, InstrumentationAttributes attributes);

    /**
     * Gets whether the instrument is enabled.
     *
     * @return {@code true} if the instrument records measurements.
     */
    boolean isEnabled();
}
