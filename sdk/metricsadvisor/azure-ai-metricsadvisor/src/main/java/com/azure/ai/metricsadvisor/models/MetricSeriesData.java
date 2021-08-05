// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.implementation.util.MetricSeriesDataHelper;

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
    private List<OffsetDateTime> timestamps;

    /*
     * values of the data related to this time series
     */
    private List<Double> metricValues;

    static {
        MetricSeriesDataHelper.setAccessor(new MetricSeriesDataHelper.MetricSeriesDataAccessor() {
            @Override
            public void setMetricId(MetricSeriesData seriesData, String metricId) {
                seriesData.setMetricId(metricId);
            }

            @Override
            public void setSeriesKey(MetricSeriesData seriesData, DimensionKey seriesKey) {
                seriesData.setSeriesKey(seriesKey);
            }

            @Override
            public void setTimestampList(MetricSeriesData seriesData, List<OffsetDateTime> timestamps) {
                seriesData.setTimestampList(timestamps);
            }

            @Override
            public void setValueList(MetricSeriesData seriesData, List<Double> metricValues) {
                seriesData.setValueList(metricValues);
            }
        });
    }

    /**
     * Get the metric unique id.
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
     * Get the timestamps of the data related to this time series.
     *
     * @return the timestamps value.
     */
    public List<OffsetDateTime> getTimestamps() {
        return this.timestamps;
    }

    /**
     * Get the values of the data related to this time series.
     *
     * @return the metricValues value.
     */
    public List<Double> getMetricValues() {
        return this.metricValues;
    }

    void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    void setSeriesKey(DimensionKey seriesKey) {
        this.seriesKey = seriesKey;
    }

    void setTimestampList(List<OffsetDateTime> timestamps) {
        this.timestamps = timestamps;
    }

    void setValueList(List<Double> metricValues) {
        this.metricValues = metricValues;
    }
}
