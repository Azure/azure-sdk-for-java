package com.azure.core.util.metrics;

import com.azure.core.util.Context;

public interface ClientLongCounter {
    /**
     * Records a value with a set of attributes.
     *
     * @param value The amount of the measurement.
     * @param context The explicit context to associate with this measurement.
     */
    void add(long value, Context context);
}
