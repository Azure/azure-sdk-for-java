// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 *
 */
@Immutable
public final class Metrics {
    private final String id;
    private final String type;
    private final MetricsUnit unit;
    private final String metricsName;
    private final List<MetricsTimeSeriesElement> timeSeries;

    /**
     * @param id
     * @param type
     * @param unit
     * @param metricsName
     * @param timeseries
     */
    public Metrics(String id, String type, MetricsUnit unit,
                   String metricsName, List<MetricsTimeSeriesElement> timeseries) {
        this.id = id;
        this.type = type;
        this.unit = unit;
        this.metricsName = metricsName;
        this.timeSeries = timeseries;
    }


    /**
     * @return
     */
    public String getMetricsName() {
        return metricsName;
    }

    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * @return
     */
    public MetricsUnit getUnit() {
        return unit;
    }

    /**
     * @return
     */
    public List<MetricsTimeSeriesElement> getTimeSeries() {
        return timeSeries;
    }
}
