// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import java.util.List;
import java.util.Map;

/**
 * A time series result type.
 */
public final class TimeSeriesElement {
    private final List<MetricValue> values;
    private final Map<String, String> metadata;

    /**
     * Creates an instance of {@link TimeSeriesElement} with a list of data points representing the metric
     * values.
     * @param values a list of data points representing the metric values.
     * @param metadata the metadata values if filter is specified in the request.
     */
    public TimeSeriesElement(List<MetricValue> values, Map<String, String> metadata) {
        this.values = values;
        this.metadata = metadata;
    }

    /**
     * Returns a list of data points representing the metric values.
     * @return a list of data points representing the metric
     */
    public List<MetricValue> getValues() {
        return values;
    }

    /**
     * Returns the metadata values if filter is specified in the request.
     * @return the metadata values if filter is specified in the request.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
