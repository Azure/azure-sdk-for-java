/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * The day of the week the job will run each month on weekly bases.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_2_0)
public enum JobScheduleMonthlyWeekDay {
    FIRST_MONDAY(1, JobScheduleDay.MONDAY),
    FIRST_TUESDAY(1, JobScheduleDay.TUESDAY),
    FIRST_WEDNESDAY(1, JobScheduleDay.WEDNESDAY),
    FIRST_THURSDAY(1, JobScheduleDay.THURSDAY),
    FIRST_FRIDAY(1, JobScheduleDay.FRIDAY),
    FIRST_SATURDAY(1, JobScheduleDay.SATURDAY),
    FIRST_SUNDAY(1, JobScheduleDay.SUNDAY),
    SECOND_MONDAY(2, JobScheduleDay.MONDAY),
    SECOND_TUESDAY(2, JobScheduleDay.TUESDAY),
    SECOND_WEDNESDAY(2, JobScheduleDay.WEDNESDAY),
    SECOND_THURSDAY(2, JobScheduleDay.THURSDAY),
    SECOND_FRIDAY(2, JobScheduleDay.FRIDAY),
    SECOND_SATURDAY(2, JobScheduleDay.SATURDAY),
    SECOND_SUNDAY(2, JobScheduleDay.SUNDAY),
    THIRD_MONDAY(3, JobScheduleDay.MONDAY),
    THIRD_TUESDAY(3, JobScheduleDay.TUESDAY),
    THIRD_WEDNESDAY(3, JobScheduleDay.WEDNESDAY),
    THIRD_THURSDAY(3, JobScheduleDay.THURSDAY),
    THIRD_FRIDAY(3, JobScheduleDay.FRIDAY),
    THIRD_SATURDAY(3, JobScheduleDay.SATURDAY),
    THIRD_SUNDAY(3, JobScheduleDay.SUNDAY),
    FOURTH_MONDAY(4, JobScheduleDay.MONDAY),
    FOURTH_TUESDAY(4, JobScheduleDay.TUESDAY),
    FOURTH_WEDNESDAY(4, JobScheduleDay.WEDNESDAY),
    FOURTH_THURSDAY(4, JobScheduleDay.THURSDAY),
    FOURTH_FRIDAY(4, JobScheduleDay.FRIDAY),
    FOURTH_SATURDAY(4, JobScheduleDay.SATURDAY),
    FOURTH_SUNDAY(4, JobScheduleDay.SUNDAY),
    LAST_MONDAY(-1, JobScheduleDay.MONDAY),
    LAST_TUESDAY(-1, JobScheduleDay.TUESDAY),
    LAST_WEDNESDAY(-1, JobScheduleDay.WEDNESDAY),
    LAST_THURSDAY(-1, JobScheduleDay.THURSDAY),
    LAST_FRIDAY(-1, JobScheduleDay.FRIDAY),
    LAST_SATURDAY(-1, JobScheduleDay.SATURDAY),
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
            case -1: {
                this.value = "Last ";
                break;
            }
            case 1: {
                this.value = "First ";
                break;
            }
            case 2: {
                this.value = "Second ";
                break;
            }
            case 3: {
                this.value = "Third ";
                break;
            }
            case 4: {
                this.value = "Fourth ";
                break;
            }
            default: {
                this.value = "";
                break;
            }
        }
        this.value += day;
    }

    /**
     * @return
     */
    public JobRecurrenceScheduleMonthlyOccurrence monthlyOccurrence() {
        return this.monthlyOccurrence;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
