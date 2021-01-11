// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Immutable;

/**
 * The MetricPeriodFeedback model.
 */
@Immutable
public final class MetricPeriodFeedback extends MetricFeedback {
    private final PeriodType periodType;
    private final int periodValue;

    /**
     * Creates an instance of MetricPeriodFeedback.
     *
     * @param periodType the type of setting period.
     * @param periodValue the number of intervals a period contains.
     */
    public MetricPeriodFeedback(PeriodType periodType,
        int periodValue) {
        this.periodType = periodType;
        this.periodValue = periodValue;
    }

    /**
     * Get the type of setting period.
     *
     * @return the periodType value.
     */
    public PeriodType getPeriodType() {
        return this.periodType;
    }

    /**
     * Get the value of the period feedback.
     * The number of intervals a period contains, when no period set to 0.
     *
     * @return the value value.
     */
    public int getPeriodValue() {
        return this.periodValue;
    }
}
