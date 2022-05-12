package com.azure.core.util.metrics;


import com.azure.core.util.Context;

import java.util.Map;

/** A histogram instrument that records {@code long} values. */
public interface DoubleHistogram {
    /**
     * Records a value with a set of attributes.
     *
     * @param value The amount of the measurement.
     * @param attributes A set of attributes to associate with the count.
     * @param context The explicit context to associate with this measurement.
     */
    void record(double value, Map<String, Object> attributes, Context context);
}
