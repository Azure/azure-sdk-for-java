// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.time.OffsetDateTime;
import java.util.List;

/** The MetricSeriesData model. */
public final class MetricSeriesData {
    /*
     * The id property.
     */
    private String metricId;

    /*
     * dimension name and value pair
     */
    private DimensionKey seriesKey;

    /*
     * timestamps of the data related to this time series
     */
    private List<OffsetDateTime> timestampList;

    /*
     * values of the data related to this time series
     */
    private List<Double> valueList;

    /**
     * Get the metricId property: metric unique id.
     *
     * @return the metricId value.
     */
    public String getMetricId() {
        return this.metricId;
    }

    /**
     * Get the dimension name and value pair.
     * <p> A {@link DimensionKey} can hold such a combination, for example,
     * [ product_category=men-shoes, city=redmond ] identifies one specific
     * time-series.
     * </p>
     * @return the seriesKey value.
     */
    public DimensionKey getSeriesKey() {
        return this.seriesKey;
    }

    /**
     * Get the timestampList property: timestamps of the data related to this time series.
     *
     * @return the timestampList value.
     */
    public List<OffsetDateTime> getTimestampList() {
        return this.timestampList;
    }

    /**
     * Get the valueList property: values of the data related to this time series.
     *
     * @return the valueList value.
     */
    public List<Double> getValueList() {
        return this.valueList;
    }
}
