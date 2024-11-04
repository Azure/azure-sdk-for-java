// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation.timewindow;

import com.azure.spring.cloud.feature.management.implementation.models.Recurrence;

import java.time.ZonedDateTime;

public class TimeWindowFilterSettings {
    /**
     * An optional start time used to determine when a feature should be enabled.
     * If no start time is specified the time window is considered to have already started.
     * */
    private ZonedDateTime start;
    /**
     * An optional end time used to determine when a feature should be disabled.
     * If no end time is specified the time window is considered to never end.
     * */
    private ZonedDateTime end;
    /**
     * Add-on recurrence rule allows the time window defined by Start and End to recur.
     * The rule specifies both how often the time window repeats and for how long.
     * */
    private Recurrence recurrence;

    /**
     * @param startTime the start time to determine when a feature should be enabled
     * */
    public void setStart(String startTime) {
        this.start = TimeWindowUtils.convertStringToDate(startTime);
    }

    /**
     * @return start time
     * */
    public ZonedDateTime getStart() {
        return start;
    }

    /**
     * @param endTime the end time to determine when a feature should be disabled
     * */
    public void setEnd(String endTime) {
        this.end = TimeWindowUtils.convertStringToDate(endTime);
    }

    /**
     * @return end time
     * */
    public ZonedDateTime getEnd() {
        return end;
    }

    /**
     * @param recurrence the recurrence rule to specify both how often the time window repeats and for how long
     * */
    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    /**
     * @return the recurrence rule
     * */
    public Recurrence getRecurrence() {
        return recurrence;
    }
}
