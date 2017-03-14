/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import org.joda.time.DateTime;

import java.util.List;

/**
 */
@Fluent
public interface Recurrence {
    /**
     * Get the frequency value.
     *
     * @return the frequency value
     */
    RecurrenceFrequency frequency();

    /**
     * Get the timeZone value.
     *
     * @return the timeZone value
     */
    String timeZone();

    /**
     * Get the days value.
     *
     * @return the days value
     */
    List<String> days();

    /**
     * Get the hours value.
     *
     * @return the hours value
     */
    List<Integer> hours();

    /**
     * Get the minutes value.
     *
     * @return the minutes value
     */
    List<Integer> minutes();

    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithRecurrentScheduleTimeZone<ParentT>,
            DefinitionStages.WithRecurrentScheduleHours<ParentT>,
            DefinitionStages.WithRecurrentScheduleMinutes<ParentT>,
            DefinitionStages.WithRecurrentScheduleDays<ParentT>,
            DefinitionStages.WithRecurrentScheduleDaysApplicable<ParentT> {
    }

    interface DefinitionStages {
        interface Blank<ParentT> {
            WithRecurrentScheduleTimeZone<ParentT> withFrequency(RecurrenceFrequency frequency);
        }

        interface WithRecurrentScheduleTimeZone<ParentT> {
            WithRecurrentScheduleHours<ParentT> withScheduleTimeZone(String scheduleTimeZone);
        }

        interface WithRecurrentScheduleHours<ParentT> {
            WithRecurrentScheduleMinutes<ParentT> withScheduleHour(int hour);
            WithRecurrentScheduleMinutes<ParentT> withScheduleHours(List<Integer> hours);
        }

        interface WithRecurrentScheduleMinutes<ParentT> {
            WithRecurrentScheduleDays<ParentT> withScheduleMinute(int minute);
            WithRecurrentScheduleDays<ParentT> withScheduleMinutes(List<Integer> minutes);
        }

        interface WithRecurrentScheduleDays<ParentT> {
            WithRecurrentScheduleDaysApplicable<ParentT> withScheduleDay(String day);
            WithRecurrentScheduleDaysApplicable<ParentT> withScheduleDays(List<String> day);
        }

        interface WithRecurrentScheduleDaysApplicable<ParentT> extends
                WithRecurrentScheduleDays<ParentT> {
            ParentT apply();
        }
    }

    interface Update<ParentT> extends
            UpdateStages.Blank<ParentT> {
    }

    interface UpdateStages {
        interface Blank<ParentT> {
            Update<ParentT> withFrequency(RecurrenceFrequency frequency);
            Update<ParentT> withScheduleTimeZone(String scheduleTimeZone);
            Update<ParentT> withScheduleHour(int hour);
            Update<ParentT> withScheduleHours(List<Integer> hours);
            Update<ParentT> withoutHour(int hour);
            Update<ParentT> withScheduleMinute(int minute);
            Update<ParentT> withScheduleMinutes(List<Integer> minutes);
            Update<ParentT> withoutScheduleMinute(int minute);
            Update<ParentT> withScheduleDay(String day);
            Update<ParentT> withScheduleDays(List<String> day);
            Update<ParentT> withoutScheduleDay(String day);
            ParentT apply();
        }
    }

}
