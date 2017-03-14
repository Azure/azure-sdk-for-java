/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
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

    StandaloneUpdateStages.Blank update();

    interface StandaloneDefinition extends
            StandaloneDefinitionStages.Blank,
            StandaloneDefinitionStages.WithRecurrentScheduleTimeZone,
            StandaloneDefinitionStages.WithRecurrentScheduleHours,
            StandaloneDefinitionStages.WithRecurrentScheduleMinutes,
            StandaloneDefinitionStages.WithRecurrentScheduleDays,
            StandaloneDefinitionStages.WithRecurrentScheduleDaysApplicable {
    }

    interface StandaloneDefinitionStages {
        interface Blank {
            WithRecurrentScheduleTimeZone withFrequency(RecurrenceFrequency frequency);
        }

        interface WithRecurrentScheduleTimeZone {
            WithRecurrentScheduleHours withScheduleTimeZone(String scheduleTimeZone);
        }

        interface WithRecurrentScheduleHours {
            WithRecurrentScheduleMinutes withScheduleHour(int hour);
            WithRecurrentScheduleMinutes withScheduleHours(List<Integer> hours);
        }

        interface WithRecurrentScheduleMinutes {
            WithRecurrentScheduleDays withScheduleMinute(int minute);
            WithRecurrentScheduleDays withScheduleMinutes(List<Integer> minutes);
        }

        interface WithRecurrentScheduleDays {
            WithRecurrentScheduleDaysApplicable withScheduleDay(String day);
            WithRecurrentScheduleDaysApplicable withScheduleDays(List<String> day);
        }

        interface WithRecurrentScheduleDaysApplicable {
            Recurrence create();
        }
    }

    interface StandaloneUpdate extends
            StandaloneUpdateStages.Blank {
    }

    interface StandaloneUpdateStages {
        interface Blank {
            StandaloneUpdate withFrequency(RecurrenceFrequency frequency);
            StandaloneUpdate withScheduleTimeZone(String scheduleTimeZone);
            StandaloneUpdate withScheduleHour(int hour);
            StandaloneUpdate withScheduleHours(List<Integer> hours);
            StandaloneUpdate withoutHour(int hour);
            StandaloneUpdate withScheduleMinute(int minute);
            StandaloneUpdate withScheduleMinutes(List<Integer> minutes);
            StandaloneUpdate withoutScheduleMinute(int minute);
            StandaloneUpdate withScheduleDay(String day);
            StandaloneUpdate withScheduleDays(List<String> day);
            StandaloneUpdate withoutScheduleDay(String day);
            Recurrence apply();
        }
    }

    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithRecurrentScheduleTimeZone,
            DefinitionStages.WithRecurrentScheduleHours,
            DefinitionStages.WithRecurrentScheduleMinutes,
            DefinitionStages.WithRecurrentScheduleDays,
            DefinitionStages.WithRecurrentScheduleDaysApplicable {
    }

    interface DefinitionStages {
        interface Blank {
            WithRecurrentScheduleTimeZone withFrequency(RecurrenceFrequency frequency);
        }

        interface WithRecurrentScheduleTimeZone {
            WithRecurrentScheduleHours withScheduleTimeZone(String scheduleTimeZone);
        }

        interface WithRecurrentScheduleHours {
            WithRecurrentScheduleMinutes withScheduleHour(int hour);
            WithRecurrentScheduleMinutes withScheduleHours(List<Integer> hours);
        }

        interface WithRecurrentScheduleMinutes {
            WithRecurrentScheduleDays withScheduleMinute(int minute);
            WithRecurrentScheduleDays withScheduleMinutes(List<Integer> minutes);
        }

        interface WithRecurrentScheduleDays {
            WithRecurrentScheduleDaysApplicable withScheduleDay(String day);
            WithRecurrentScheduleDaysApplicable withScheduleDays(List<String> day);
        }

        interface WithRecurrentScheduleDaysApplicable extends
                Attachable.InDefinition<AutoscaleProfile.DefinitionStages.WithScaleRuleOptional> {
        }
    }

    interface UpdateDefinition extends
            UpdateDefinitionStages.Blank,
            UpdateDefinitionStages.WithRecurrentScheduleTimeZone,
            UpdateDefinitionStages.WithRecurrentScheduleHours,
            UpdateDefinitionStages.WithRecurrentScheduleMinutes,
            UpdateDefinitionStages.WithRecurrentScheduleDays,
            UpdateDefinitionStages.WithRecurrentScheduleDaysApplicable {
    }

    interface UpdateDefinitionStages {
        interface Blank {
            WithRecurrentScheduleTimeZone withFrequency(RecurrenceFrequency frequency);
        }

        interface WithRecurrentScheduleTimeZone {
            WithRecurrentScheduleHours withScheduleTimeZone(String scheduleTimeZone);
        }

        interface WithRecurrentScheduleHours {
            WithRecurrentScheduleMinutes withScheduleHour(int hour);
            WithRecurrentScheduleMinutes withScheduleHours(List<Integer> hours);
        }

        interface WithRecurrentScheduleMinutes {
            WithRecurrentScheduleDays withScheduleMinute(int minute);
            WithRecurrentScheduleDays withScheduleMinutes(List<Integer> minutes);
        }

        interface WithRecurrentScheduleDays {
            WithRecurrentScheduleDaysApplicable withScheduleDay(String day);
            WithRecurrentScheduleDaysApplicable withScheduleDays(List<String> day);
        }

        interface WithRecurrentScheduleDaysApplicable extends
                Attachable.InUpdate<AutoscaleProfile.Update> {
        }
    }

    interface Update extends
            Settable<AutoscaleProfile.Update>,
            UpdateStages.Blank {
    }

    interface UpdateStages {
        interface Blank {
            Update withFrequency(RecurrenceFrequency frequency);
            Update withScheduleTimeZone(String scheduleTimeZone);
            Update withScheduleHour(int hour);
            Update withScheduleHours(List<Integer> hours);
            Update withoutHour(int hour);
            Update withScheduleMinute(int minute);
            Update withScheduleMinutes(List<Integer> minutes);
            Update withoutScheduleMinute(int minute);
            Update withScheduleDay(String day);
            Update withScheduleDays(List<String> day);
            Update withoutScheduleDay(String day);
        }
    }

}
