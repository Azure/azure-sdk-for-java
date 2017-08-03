/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler;

import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.scheduler.implementation.ScheduleServiceManager;
import org.joda.time.DateTime;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.scheduler.implementation.JobDefinitionInner;

/**
 * An immutable client-side representation of a job resource for the Azure Scheduler service.
 */
@Fluent()
@Beta(Beta.SinceVersion.V1_2_0)
public interface Job extends
        IndependentChild<ScheduleServiceManager>,
        Updatable<Job.Update>,
        Indexable,
        HasInner<JobDefinitionInner> {

    /***********************************************************
     * Getters
     ***********************************************************/

    /**
     * @return the job collection name for wich the job belongs to
     */
    String jobCollectionName();

    /**
     * @return the job's start time
     */
    DateTime startTime();

    /**
     * @return the job action type
     */
    JobAction action();

    /**
     * @return the the frequency of recurrence for the job
     */
    JobRecurrence recurrence();

    /**
     * @return the state of the job
     */
    JobState state();

    /**
     * @return the job's status
     */
    JobStatus status();

    /***********************************************************
     * Actions
     ***********************************************************/


    /**
     * The entirety of the Azure Scheduler job definition.
     */
    interface Definition extends
        Job.DefinitionStages.Blank,
        Job.DefinitionStages.WithStartTime,
        Job.DefinitionStages.WithRecurrenceOrAction,
        Job.DefinitionStages.WithRecurrenceFrequencyAndSchedule,
        Job.DefinitionStages.WithAdvanceDaysOfWeekSchedule,
        Job.DefinitionStages.WithAdvanceDaysOfMonthSchedule,
        Job.DefinitionStages.WithAdvanceMinutesAndHoursSchedule,
        Job.DefinitionStages.WithEndTime,
        Job.DefinitionStages.WithAction,
        Job.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Azure Scheduler job definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a job definition.
         */
        interface Blank extends WithStartTime {
        }

        /**
         * The stage of the job definition for an Azure Scheduler allowing to specify the start time.
         */
        interface WithStartTime {
            /**
             * Specifies that the job should be started right away.
             *
             * @return the next stage of the definition
             */
            WithRecurrenceOrAction startingNow();

            /**
             * Specifies the start time of the job.
             *
             * @param startTime the start time
             * @return the next stage of the definition
             */
            WithRecurrenceOrAction startingAt(DateTime startTime);
        }

        /**
         * The stage of the job definition for an Azure Scheduler allowing to specify the recurrence and action details.
         */
        interface WithRecurrenceOrAction extends WithAction {
            /**
             * The recurrence object specifies recurrence rules for the job and the recurrence the job will execute with.
             * <p>
             * The recurrent object allows the frequency, the end time, the number of occurrences and the schedule
             *   to be specified. If a recurrence is defined, at least the frequency should be specified,
             *   the rest is optional.
             *
             * @param jobRecurrence the reccurence object
             * @return the next stage of the definition
             */
            WithAction withRecurrence(JobRecurrence jobRecurrence);

            /**
             * Specifies the number of time units between consecutive occurrences. The exact time unit to use is specified at the next stage.
             * <p>
             * For example, if 3 is specified here and "week" is selected at the next stage, the job will recur every 3 weeks.
             * <p>
             * Different time units support different maximum numbers of occurrences.
             *   Those maximum combinations are: 18 months, 78 weeks, 548 days, 1000 hours or minutes.
             *
             * @param interval a positive integer and denotes the interval for the frequency that determines how often the job will run
             * @return the next stage of the definition
             */
            WithRecurrenceFrequencyAndSchedule runningEvery(int interval);

            /**
             * Specifies the recurrence of the job to be every minute.
             *
             * @return the next stage of the definition
             */
            WithEndTime runningEveryMinute();

            /**
             * Specifies the recurrence of the job to be every hour.
             *
             * @return the next stage of the definition
             */
            WithEndTime runningHourly();

            /**
             * Specifies the recurrence of the job to be every day.
             *
             * @return the next stage of the definition
             */
            WithAdvanceMinutesAndHoursSchedule runningDaily();

            /**
             * Specifies the recurrence  of the job to be every week.
             *
             * @return the next stage of the definition
             */
            WithAdvanceDaysOfWeekSchedule runningWeekly();

            /**
             * Specifies the recurrence of the job to be every month.
             *
             * @return the next stage of the definition
             */
            WithAdvanceDaysOfMonthSchedule runningMonthly();
        }

        /**
         * The stage of the job definition for an Azure Scheduler allowing to specify the frequency unit.
         */
        interface WithRecurrenceFrequencyAndSchedule {
            /**
             * Selects minutes as the time unit for the number of occurrences.
             *
             * @return the next stage of the definition
             */
            WithEndTime minutes();

            /**
             * Selects hours as the time unit for the number of occurrences.
             *
             * @return the next stage of the definition
             */
            WithEndTime hours();

            /**
             * Specifies the recurrence of the job to be every day.
             *
             * @return the next stage of the definition
             */
            WithAdvanceMinutesAndHoursSchedule days();

            /**
             * Selects weeks as the time unit for the number of occurrences.
             *
             * @return the next stage of the definition
             */
            WithAdvanceDaysOfWeekSchedule weeks();

            /**
             * Selects months as the time unit for the number of occurrences.
             *
             * @return the next stage of the definition
             */
            WithAdvanceDaysOfMonthSchedule months();
        }

        /**
         * The stage of the job definition for an Azure Scheduler allowing to specify advance options for week to week execution times.
         */
        interface WithAdvanceDaysOfWeekSchedule extends WithEndTime {
            /**
             * Specifies the days of the week when the job will run.
             *
             * @param days the job will run an the days of the month specified
             * @return the next stage of the definition
             */
            WithAdvanceMinutesAndHoursSchedule onTheseDays(DayOfWeek... days);
        }

        /**
         * The stage of the job definition for an Azure Scheduler allowing to specify advance options for month to month execution times.
         */
        interface WithAdvanceDaysOfMonthSchedule extends WithEndTime {
            /**
             * Specifies the days of the month when the job will run.
             * <p>
             * Use -1 for the last day of a month.
             *
             * @param days the job will run an the days of the month specified
             * @return the next stage of the definition
             */
            WithAdvanceMinutesAndHoursSchedule onTheseDaysOfMonth(int... days);

            /**
             * Specifies the week days of the month when the job will run.
             *
             * @param weekDays the job will run at each week day of the month specified
             * @return the next stage of the definition
             */
            WithAdvanceMinutesAndHoursSchedule recurringEvery(JobScheduleMonthlyWeekDay... weekDays);

            /**
             * Specifies the week days of the month when the job will run.
             *
             * @param weekDays the job will run at each week day of the month specified
             * @return the next stage of the definition
             */
            WithAdvanceMinutesAndHoursSchedule recurringEvery(JobScheduleDay... weekDays);
        }

        /**
         * The stage of the job definition for an Azure Scheduler allowing to specify advance options for within 24 hours time.
         */
        interface WithAdvanceMinutesAndHoursSchedule extends WithEndTime {
            /**
             * Specifies the minutes of the hour at which the job will run.
             *
             * @param minutes the job will run at each minute specified
             * @return the next stage of the definition
             */
            WithAdvanceMinutesAndHoursSchedule atTheseMinutes(int... minutes);

            /**
             * Specifies the hours of the day at which the job will run.
             * <p>
             * Hours are specified in 24 hour time.
             *
             * @param hours the job will run at each hour (and minute) specified
             * @return the next stage of the definition
             */
            WithAdvanceMinutesAndHoursSchedule atTheseHours(int... hours);
        }

        /**
         * The stage of the job definition for an Azure Scheduler allowing to specify when the job scheduling ends.
         */
        interface WithEndTime extends WithAction {
            /**
             * Specifies the job to run indefinitely.
             *
             * @return the next stage of the definition
             */
            WithAction endingNever();

            /**
             * Specifies the time after which the job will no longer be started automatically.
             *
             * @param endTime number of times the job will run
             * @return the next stage of the definition
             */
            WithAction endingBy(DateTime endTime);

            /**
             * Specifies the specified number of runs after which the job will no longer be started automatically.
             *
             * @param numberOfOccurences number of times the job will run
             * @return the next stage of the definition
             */
            WithAction endingAfterOccurrence(int numberOfOccurences);
        }

        /**
         * The stage of the job definition for an Azure Scheduler allowing to specify the action details.
         */
        interface WithAction {
            /**
             * Specifies the action details of the job.
             *
             * @param jobAction the action details
             * @return the next stage of the definition
             */
            WithCreate withAction(JobAction jobAction);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<Job> {
        }
    }

    /**
     * The template for Azure Scheduler job update operation, containing all the settings that can be modified.
     */
    interface Update extends
        UpdateStages.WithState {
    }

    /**
     * Grouping of Azure Scheduler job update stages.
     */
    interface UpdateStages {

        /**
         * Grouping of Azure Scheduler job update optionals.
         */
        interface UpdateOptionals extends
            Appliable<Job>,
            WithStartTime,
            WithRecurrence,
            WithAction {
        }

        /**
         * The stage of the job update for an Azure Scheduler allowing to modify the start time.
         */
        interface WithStartTime {
            /**
             * Specifies the start time of the job.
             *
             * @param startTime the start time
             * @return the next stage of the definition
             */
            UpdateOptionals withStartTime(DateTime startTime);
        }

        /**
         * The stage of the job update for an Azure Scheduler allowing to modify the recurrence.
         */
        interface WithRecurrence {
            /**
             * Specifies the recurrence  of the job.
             *
             * @param jobRecurrence the start time
             * @return the next stage of the definition
             */
            UpdateOptionals withRecurrence(JobRecurrence jobRecurrence);
        }

        /**
         * The stage of the job update for an Azure Scheduler allowing to modify the action details.
         */
        interface WithAction {
            /**
             * Specifies the action details of the job.
             *
             * @param jobAction the action details
             * @return the next stage of the definition
             */
            UpdateOptionals withAction(JobAction jobAction);
        }

        /**
         * The stage of the Job update allowing to modify the state.
         */
        interface WithState {
            /**
             * Specifies the state of the Job.
             * <p>
             * Required as part of the update flow because the job's state value can be "COMPLETED",
             *   in which case the update can not be performed.
             *
             * @param state the job state
             * @return the next stage of the definition
             */
            UpdateOptionals withState(JobState state);
        }
    }
}

