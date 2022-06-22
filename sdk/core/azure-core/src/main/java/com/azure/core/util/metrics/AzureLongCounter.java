// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AzureAttributeCollection;
import com.azure.core.util.Context;

/**
 * A counter instrument that records {@code long} values.
 *
 * <p>Counters only allow adding positive values, and guarantee the resulting metrics will be
 * always-increasing monotonic sums.
 */
public interface AzureLongCounter {
    /**
     * Records a value with a set of attributes.
     *
     * @param value The amount of the measurement.
     * @param attributes Collection of attributes representing metric dimensions.
     * @param context The explicit context to associate with this measurement.
     */
    void add(long value, AzureAttributeCollection attributes, Context context);
}
