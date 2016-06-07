/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;

/**
 * Specifies how tasks should be run in a job associated with a job schedule.
 */
public class JobScheduleExecutionInformation {
    /**
     * The next time at which a job will be created under this schedule.
     */
    private DateTime nextRunTime;

    /**
     * Information about the most recent job under the job schedule.
     */
    private RecentJob recentJob;

    /**
     * The time at which the schedule ended. This property is set only if the
     * job schedule is in the completed state.
     */
    private DateTime endTime;

    /**
     * Get the nextRunTime value.
     *
     * @return the nextRunTime value
     */
    public DateTime nextRunTime() {
        return this.nextRunTime;
    }

    /**
     * Set the nextRunTime value.
     *
     * @param nextRunTime the nextRunTime value to set
     * @return the JobScheduleExecutionInformation object itself.
     */
    public JobScheduleExecutionInformation withNextRunTime(DateTime nextRunTime) {
        this.nextRunTime = nextRunTime;
        return this;
    }

    /**
     * Get the recentJob value.
     *
     * @return the recentJob value
     */
    public RecentJob recentJob() {
        return this.recentJob;
    }

    /**
     * Set the recentJob value.
     *
     * @param recentJob the recentJob value to set
     * @return the JobScheduleExecutionInformation object itself.
     */
    public JobScheduleExecutionInformation withRecentJob(RecentJob recentJob) {
        this.recentJob = recentJob;
        return this;
    }

    /**
     * Get the endTime value.
     *
     * @return the endTime value
     */
    public DateTime endTime() {
        return this.endTime;
    }

    /**
     * Set the endTime value.
     *
     * @param endTime the endTime value to set
     * @return the JobScheduleExecutionInformation object itself.
     */
    public JobScheduleExecutionInformation withEndTime(DateTime endTime) {
        this.endTime = endTime;
        return this;
    }

}
