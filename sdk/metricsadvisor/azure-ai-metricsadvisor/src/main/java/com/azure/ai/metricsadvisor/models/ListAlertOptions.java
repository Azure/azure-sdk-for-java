// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.time.OffsetDateTime;

/**
 * Describes the additional parameters for the API to list the alerts triggered.
 */
public final class ListAlertOptions {
    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;
    private final TimeMode timeMode;
    private Integer top;
    private Integer skip;

    /**
     * Creates a new instance of ListAlertOptions.
     *
     * @param startTime The start time of the time range within which the alerts were triggered.
     * @param endTime The end time of the time range within which the alerts were triggered.
     * @param timeMode The time mode.
     */
    public ListAlertOptions(OffsetDateTime startTime,
                            OffsetDateTime endTime,
                            TimeMode timeMode) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeMode = timeMode;
    }

    /**
     * Gets the start time of the time range within which the alerts were triggered.
     *
     * @return The start time.
     */
    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Gets the end time of the time range within which the alerts were triggered.
     *
     * @return The end time.
     */
    public OffsetDateTime getEndTime() {
        return this.endTime;
    }

    /**
     * Gets the time model.
     *
     * @return The time mode.
     */
    public TimeMode getTimeMode() {
        return this.timeMode;
    }

    /**
     * Gets limit indicating the number of items to be included in a service returned page.
     *
     * @return The top value.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Gets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @return The skip value.
     */
    public Integer getSkip() {
        return this.skip;
    }

    /**
     * Sets limit indicating the number of items to be included in a service returned page.
     *
     * @param top The skip value.
     * @return ListAlertOptions itself.
     */
    public ListAlertOptions setTop(int top) {
        this.top = top;
        return this;
    }

    /**
     * Sets the number of items in the queried collection that are to be skipped and not included
     * in the returned result.
     *
     * @param skip The skip value.
     * @return ListAlertOptions itself.
     */
    public ListAlertOptions setSkip(int skip) {
        this.skip = skip;
        return this;
    }
}
