// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clientTelemetry;

import java.util.HashMap;
import java.util.Map;

public class MetricInfo {
    private String metricsName;
    private String unitName;
    private double sum;
    private long count;
    private double min;
    private double max;
    private Map<Float, Float> percentiles = new HashMap<>();//strict contract, ,  validation on key required

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

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
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

    public Map<Float, Float> getPercentiles() {
        return percentiles;
    }

    public void setPercentiles(Map<Float, Float> percentiles) {
        this.percentiles = percentiles;
    }
}
