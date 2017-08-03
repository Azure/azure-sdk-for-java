/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * Days of the week relative to the beginning of the month for scheduling a job.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_2_0)
public enum JobScheduleMonthlyWeekDay {
    /** Enum value First Monday. */
    FIRST_MONDAY(1, JobScheduleDay.MONDAY),
    /** Enum value First Tuesday. */
    FIRST_TUESDAY(1, JobScheduleDay.TUESDAY),
    /** Enum value First Wednesday. */
    FIRST_WEDNESDAY(1, JobScheduleDay.WEDNESDAY),
    /** Enum value First Thursday. */
    FIRST_THURSDAY(1, JobScheduleDay.THURSDAY),
    /** Enum value First Friday. */
    FIRST_FRIDAY(1, JobScheduleDay.FRIDAY),
    /** Enum value First Saturday. */
    FIRST_SATURDAY(1, JobScheduleDay.SATURDAY),
    /** Enum value First Sunday. */
    FIRST_SUNDAY(1, JobScheduleDay.SUNDAY),
    /** Enum value Second Monday. */
    SECOND_MONDAY(2, JobScheduleDay.MONDAY),
    /** Enum value Second Tuesday. */
    SECOND_TUESDAY(2, JobScheduleDay.TUESDAY),
    /** Enum value Second Wednesday. */
    SECOND_WEDNESDAY(2, JobScheduleDay.WEDNESDAY),
    /** Enum value Second Thursday. */
    SECOND_THURSDAY(2, JobScheduleDay.THURSDAY),
    /** Enum value Second Friday. */
    SECOND_FRIDAY(2, JobScheduleDay.FRIDAY),
    /** Enum value Second Saturday. */
    SECOND_SATURDAY(2, JobScheduleDay.SATURDAY),
    /** Enum value Second Sunday. */
    SECOND_SUNDAY(2, JobScheduleDay.SUNDAY),
    /** Enum value Third Monday. */
    THIRD_MONDAY(3, JobScheduleDay.MONDAY),
    /** Enum value Third Tuesday. */
    THIRD_TUESDAY(3, JobScheduleDay.TUESDAY),
    /** Enum value Third Wednesday. */
    THIRD_WEDNESDAY(3, JobScheduleDay.WEDNESDAY),
    /** Enum value Third Thursday. */
    THIRD_THURSDAY(3, JobScheduleDay.THURSDAY),
    /** Enum value Third Friday. */
    THIRD_FRIDAY(3, JobScheduleDay.FRIDAY),
    /** Enum value Third Saturday. */
    THIRD_SATURDAY(3, JobScheduleDay.SATURDAY),
    /** Enum value Third Sunday. */
    THIRD_SUNDAY(3, JobScheduleDay.SUNDAY),
    /** Enum value Fourth Monday. */
    FOURTH_MONDAY(4, JobScheduleDay.MONDAY),
    /** Enum value Fourth Tuesday. */
    FOURTH_TUESDAY(4, JobScheduleDay.TUESDAY),
    /** Enum value Fourth Wednesday. */
    FOURTH_WEDNESDAY(4, JobScheduleDay.WEDNESDAY),
    /** Enum value Fourth Thursday. */
    FOURTH_THURSDAY(4, JobScheduleDay.THURSDAY),
    /** Enum value Fourth Friday. */
    FOURTH_FRIDAY(4, JobScheduleDay.FRIDAY),
    /** Enum value Fourth Saturday. */
    FOURTH_SATURDAY(4, JobScheduleDay.SATURDAY),
    /** Enum value Fourth Sunday. */
    FOURTH_SUNDAY(4, JobScheduleDay.SUNDAY),
    /** Enum value Last Monday. */
    LAST_MONDAY(-1, JobScheduleDay.MONDAY),
    /** Enum value Last Tuesday. */
    LAST_TUESDAY(-1, JobScheduleDay.TUESDAY),
    /** Enum value Last Wednesday. */
    LAST_WEDNESDAY(-1, JobScheduleDay.WEDNESDAY),
    /** Enum value Last Thursday. */
    LAST_THURSDAY(-1, JobScheduleDay.THURSDAY),
    /** Enum value Last Friday. */
    LAST_FRIDAY(-1, JobScheduleDay.FRIDAY),
    /** Enum value Last Saturday. */
    LAST_SATURDAY(-1, JobScheduleDay.SATURDAY),
    /** Enum value Last Sunday. */
    LAST_SUNDAY(-1, JobScheduleDay.SUNDAY);

    /** The actual serialized value for a JobScheduleMonthlyWeekDay instance. */
    private String value;

    private JobRecurrenceScheduleMonthlyOccurrence monthlyOccurrence;

    /**
     * @param occurrence the week of the moth the job will run; -1 for last week in the month
     * @param day the day of the week the job will run
     */
    JobScheduleMonthlyWeekDay(int occurrence, JobScheduleDay day) {
        monthlyOccurrence = new JobRecurrenceScheduleMonthlyOccurrence().withOccurrence(occurrence).withDay(day);

        switch (occurrence) {
            case -1:
                this.value = "Last ";
                break;
            case 1:
                this.value = "First ";
                break;
            case 2:
                this.value = "Second ";
                break;
            case 3:
                this.value = "Third ";
                break;
            case 4:
                this.value = "Fourth ";
                break;
            default:
                this.value = "";
                break;
        }
        this.value += day;
    }

    /**
     * @return monthly occurrence
     */
    public JobRecurrenceScheduleMonthlyOccurrence monthlyOccurrence() {
        return this.monthlyOccurrence;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
