// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * The type TopNGroupScope represents the parameters that defines TopN anomaly scope.
 * Detector produces an alert when such "scoped" anomalies are detected.
 * <p>
 * Each metric has a stream of data points. There can be multiple metrics, hence multiple streams
 * of data points. When these multiple streams produce data points, it can be form a "set" stream.
 * Each set (aka point-set) is indexed by a timestamp; such a set contains data points information
 * arrived at that timestamp from different streams. The TopNGroupScope parameters defines the
 * criteria to report anomalies from these point-set. Each data point in the point-set can be either
 * an anomaly or not. Each anomaly has a rank assigned. The detector can look into the rank of anomalies
 * in each point-sets and use them to compute anomaly-rank for the point-set.
 * The Period in TopNGroupScope means how many latest point-sets we want the service to select for ranking.
 * The Top in TopNGroupScope defines the top anomaly-rank (like top 3 rank), and when anomaly-rank
 * of MinTopCount number of point-sets falls into this top rank, the detector to produce an alert.
 */
@Fluent
public final class TopNGroupScope {
    private Integer top;
    private Integer period;
    private Integer minTopCount;

    /**
     * Create an instance of TopNGroupScope describing parameters that defines TopN anomaly scope.
     *
     * @param top the top anomaly-rank.
     * @param period the number of latest point-sets detector select for ranking.
     * @param minTopCount the number of point-sets in the {@code period} to falls into the top rank for
     *     anomaly reporting.
     */
    public TopNGroupScope(int top, int period, int minTopCount) {
        this.top = top;
        this.period = period;
        this.minTopCount = minTopCount;
    }

    /**
     * Gets the top anomaly-rank.
     *
     * @return the top value.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Gets the period indicating the number of latest point-sets detector select for ranking.
     *
     * @return the period value.
     */
    public Integer getPeriod() {
        return this.period;
    }

    /**
     * Gets the number of point-sets in the period to falls into the top rank for anomaly reporting.
     *
     * @return the minTopCount value.
     */
    public Integer getMinTopCount() {
        return this.minTopCount;
    }

    /**
     * Sets the top anomaly-rank value, value should in the range : [1, +∞).
     *
     * @param top the top value to set.
     * @return the TopNGroupScope object itself.
     */
    public TopNGroupScope setTop(Integer top) {
        this.top = top;
        return this;
    }


    /**
     * Sets the period value indicating the number of latest point-sets detector should select for ranking,
     * value should be in the range : [1, +∞).
     *
     * @param period the period value to set.
     * @return the TopNGroupScope object itself.
     */
    public TopNGroupScope setPeriod(Integer period) {
        this.period = period;
        return this;
    }


    /**
     * Sets the number of point-sets in the period to falls into the top rank for anomaly reporting,
     *
     * <p>
     * the value should be less than or equal to period.
     *
     * @param minTopCount the minTopCount value to set.
     * @return the TopNGroupScope object itself.
     */
    public TopNGroupScope setMinTopCount(Integer minTopCount) {
        this.minTopCount = minTopCount;
        return this;
    }
}
