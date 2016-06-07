/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * The schedule according to which jobs will be created.
 */
public class Schedule {
    /**
     * The earliest time at which any job may be created under this job
     * schedule. If you do not specify a doNotRunUntil time, the schedule
     * becomes ready to create jobs immediately.
     */
    private DateTime doNotRunUntil;

    /**
     * A time after which no job will be created under this job schedule. The
     * schedule will move to the completed state as soon as this deadline is
     * past and there is no active job under this job schedule.
     */
    private DateTime doNotRunAfter;

    /**
     * The time interval, starting from the time at which the schedule
     * indicates a job should be created, within which a job must be created.
     * If a job is not created within the startWindow interval, then the
     * 'opportunity' is lost; no job will be created until the next
     * recurrence of the schedule.
     */
    private Period startWindow;

    /**
     * The time interval between the start times of two successive jobs under
     * the job schedule. A job schedule can have at most one active job under
     * it at any given time.
     */
    private Period recurrenceInterval;

    /**
     * Get the doNotRunUntil value.
     *
     * @return the doNotRunUntil value
     */
    public DateTime doNotRunUntil() {
        return this.doNotRunUntil;
    }

    /**
     * Set the doNotRunUntil value.
     *
     * @param doNotRunUntil the doNotRunUntil value to set
     * @return the Schedule object itself.
     */
    public Schedule withDoNotRunUntil(DateTime doNotRunUntil) {
        this.doNotRunUntil = doNotRunUntil;
        return this;
    }

    /**
     * Get the doNotRunAfter value.
     *
     * @return the doNotRunAfter value
     */
    public DateTime doNotRunAfter() {
        return this.doNotRunAfter;
    }

    /**
     * Set the doNotRunAfter value.
     *
     * @param doNotRunAfter the doNotRunAfter value to set
     * @return the Schedule object itself.
     */
    public Schedule withDoNotRunAfter(DateTime doNotRunAfter) {
        this.doNotRunAfter = doNotRunAfter;
        return this;
    }

    /**
     * Get the startWindow value.
     *
     * @return the startWindow value
     */
    public Period startWindow() {
        return this.startWindow;
    }

    /**
     * Set the startWindow value.
     *
     * @param startWindow the startWindow value to set
     * @return the Schedule object itself.
     */
    public Schedule withStartWindow(Period startWindow) {
        this.startWindow = startWindow;
        return this;
    }

    /**
     * Get the recurrenceInterval value.
     *
     * @return the recurrenceInterval value
     */
    public Period recurrenceInterval() {
        return this.recurrenceInterval;
    }

    /**
     * Set the recurrenceInterval value.
     *
     * @param recurrenceInterval the recurrenceInterval value to set
     * @return the Schedule object itself.
     */
    public Schedule withRecurrenceInterval(Period recurrenceInterval) {
        this.recurrenceInterval = recurrenceInterval;
        return this;
    }

}
