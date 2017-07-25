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
             * Specifies the start time of the job.
             *
             * @param startTime the start time
             * @return the next stage of the definition
             */
            WithRecurrenceOrAction withStartTime(DateTime startTime);
        }

        /**
         * The stage of the job definition for an Azure Scheduler allowing to specify the recurrence and action details.
         */
        interface WithRecurrenceOrAction {
            /**
             * Specifies the recurrence  of the job.
             *
             * @param jobRecurrence the start time
             * @return the next stage of the definition
             */
            WithAction withRecurrence(JobRecurrence jobRecurrence);

            /**
             * Specifies the action details of the job.
             *
             * @param jobAction the action details
             * @return the next stage of the definition
             */
            WithCreate withAction(JobAction jobAction);
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
        Appliable<Job>,
        UpdateStages.WithStartTime,
        UpdateStages.WithRecurrence,
        UpdateStages.WithAction,
        UpdateStages.WithState {
    }

    /**
     * Grouping of Azure Scheduler job update stages.
     */
    interface UpdateStages {

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
            Update withStartTime(DateTime startTime);
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
            Update withRecurrence(JobRecurrence jobRecurrence);
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
            Update withAction(JobAction jobAction);
        }

        /**
         * The stage of the Job update allowing to modify the state.
         */
        interface WithState {
            /**
             * Specifies the state of the Job.
             *
             * @param state the job state
             * @return the next stage of the definition
             */
            Update withState(JobState state);
        }
    }
}

