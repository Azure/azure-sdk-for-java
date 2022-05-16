package com.azure.core.util.metrics;


import com.azure.core.util.Context;

/** A histogram instrument that records {@code long} values. */
public interface ClientLongHistogram {
    /**
     * Records a value with a set of attributes.
     *
     * @param value The amount of the measurement.
     * @param context The explicit context to associate with this measurement.
     */
    void record(long value, Context context);
}
