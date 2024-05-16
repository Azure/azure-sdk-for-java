// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricInfo {
    private String metricsName;
    private String unitName;
    private double mean;
    private long count;
    private double min;
    private double max;
    private Map<Double, Double> percentiles;

    public MetricInfo(String metricsName, String unitName) {
        this.metricsName = metricsName;
        this.unitName = unitName;
    }

    public String getMetricsName() {
        return metricsName;
    }

    public void setMetricsName(String metricsName) {
        this.metricsName = metricsName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public Map<Double, Double> getPercentiles() {
        return percentiles;
    }

    public void setPercentiles(Map<Double, Double> percentiles) {
        this.percentiles = percentiles;
    }
}
