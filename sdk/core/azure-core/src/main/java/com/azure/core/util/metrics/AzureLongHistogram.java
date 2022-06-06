// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AttributeBuilder;
import com.azure.core.util.Context;

/** A histogram instrument that records {@code long} values. */
public interface AzureLongHistogram {
    /**
     * Records a value with a set of attributes.
     *
     * @param value The amount of the measurement.
     * @param context The explicit context to associate with this measurement.
     */
    void record(long value, AttributeBuilder attributes, Context context);
}
