// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import java.util.List;

/**
 *
 */
public final class MetricsTimeSeriesElement {
    private final List<MetricsValue> data;

    /**
     * @param data
     */
    public MetricsTimeSeriesElement(List<MetricsValue> data) {
        this.data = data;
    }

    /**
     * @return
     */
    public List<MetricsValue> getData() {
        return data;
    }
}
