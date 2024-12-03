// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Type that describes suppress condition for anomalies.
 */
@Fluent
public final class SuppressCondition {
    private Integer minNumber;
    private Double minRatio;

    /**
     * Create an instance of SuppressCondition describing how to suppress anomaly reporting.
     *
     * <p> Anomalies from metrics with higher granularity can be noisy, using SuppressCondition
     * user can inform the detector to not to report the anomalies until {@code minRatio}
     * percentage of last {@code minNumber} data points are detected as anomalies.
     *
     * @param minNumber the number of latest data points to consider for detection.
     * @param minRatio the percentage of the {@code minNumber} data points to be anomalies before reporting.
     */
    public SuppressCondition(int minNumber, double minRatio) {
        this.minNumber = minNumber;
        this.minRatio = minRatio;
    }

    /**
     * Gets the number of latest data points to consider for detection.
     *
     * @return the minimum number value.
     */
    public Integer getMinNumber() {
        return this.minNumber;
    }

    /**
     * Gets the percentage of the {@code minNumber} data points to be anomalies before reporting.
     *
     * @return the minimum ratio value.
     */
    public Double getMinRatio() {
        return this.minRatio;
    }

    /**
     * Sets the number of latest data points to consider for detection, the value should
     * be in the range : [1, +∞).
     *
     * @param minNumber the minNumber value to set.
     * @return the SuppressCondition object itself.
     */
    public SuppressCondition setMinNumber(Integer minNumber) {
        this.minNumber = minNumber;
        return this;
    }


    /**
     * Sets the percentage of the {@code minNumber} data points to be anomalies before reporting,
     * the value should be in the range : (0, 100].
     *
     * @param minRatio the minRatio value to set.
     * @return the SuppressCondition object itself.
     */
    public SuppressCondition setMinRatio(Double minRatio) {
        this.minRatio = minRatio;
        return this;
    }
}
