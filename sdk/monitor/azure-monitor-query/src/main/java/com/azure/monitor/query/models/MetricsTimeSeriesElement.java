// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import java.util.List;

/**
 * A time series result type.
 */
public final class MetricsTimeSeriesElement {
    private final List<MetricsValue> data;

    /**
     * Creates an instance of {@link MetricsTimeSeriesElement} with a list of data points representing the metric
     * values.
     * @param data a list of data points representing the metric values.
     */
    public MetricsTimeSeriesElement(List<MetricsValue> data) {
        this.data = data;
    }

    /**
     * Returns a list of data points representing the metric values.
     * @return a list of data points representing the metric
     */
    public List<MetricsValue> getData() {
        return data;
    }
}
