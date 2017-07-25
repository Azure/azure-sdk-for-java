/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.scheduler.implementation.JobCollectionDefinitionInner;
import com.microsoft.azure.management.scheduler.implementation.ScheduleServiceManager;
import rx.Completable;

/**
 * An immutable client-side representation of a Job Collection within Azure Scheduler service.
 */
@Fluent()
@Beta(Beta.SinceVersion.V1_2_0)
public interface JobCollection extends
    GroupableResource<ScheduleServiceManager, JobCollectionDefinitionInner>,
    Refreshable<JobCollection>,
    Updatable<JobCollection.Update> {

    /***********************************************************
     * Getters
     ***********************************************************/

    /**
     * @return the SKU type of the job collection
     */
    Sku sku();

    /**
     * @return the state of the job collection
     */
    JobCollectionState state();

    /**
     * @return the job collection quota; returns null if no quota was set
     */
    JobCollectionQuota quota();

    /**
     * @return the maximum job count in the job collection
     */
    int maxJobCount();

    /**
     * @return the maximum recurrence frequency for the jobs in the job collection
     */
    RecurrenceFrequency maxRecurrenceFrequency();

    /**
     * @return entry point to manage jobs within the job collection for an Azure Scheduler
     */
    Jobs jobs();

    /***********************************************************
     * Actions
     ***********************************************************/

    /**
     * Enables all of the jobs in the job collection.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void enable();

    /**
     * Enables all of the jobs in the job collection.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Completable enableAsync();

    /**
     * Disables all of the jobs in the job collection.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @throws CloudException thrown if the request is rejected by server
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent
     */
    void disable();

    /**
     * Disables all of the jobs in the job collection.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Completable disableAsync();


    /**
     * The entirety of the Job Collection definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithSku,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of virtual network definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the Job Collection definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the Job Collection definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSku> {
        }

        /**
         * Specifies the SKU of the Job Collection for an Azure Scheduler service.
         */
        interface WithSku {
            /**
             * Specifies the SKU type for the Job Collection.
             *
             * @param skuDefinition the SKU type
             * @return the next stage of the definition
             */
            WithCreate withSku(SkuDefinition skuDefinition);
        }

        /**
         * Specifies the Job Collection quota for an Azure Scheduler service.
         */
        interface WithJobCollectionQuota {
            /**
             * Specifies the SKU type for the availability set.
             *
             * @param quota the quota object; if null, set the quota values to default for the current SKU
             * @return the next stage of the definition
             */
            WithCreate withJobCollectionQuota(JobCollectionQuota quota);

            /**
             * Specifies the SKU type for the availability set.
             *
             * @param maxJobCount the maximum job count
             * @param maxJobOccurrence the maximum job occurrence
             * @param recurrenceFrequency the frequency of recurrence
             * @param retriesInterval the interval between retries
             * @return the next stage of the definition
             */
            WithCreate withJobCollectionQuota(int maxJobCount, int maxJobOccurrence, RecurrenceFrequency recurrenceFrequency, int retriesInterval);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         *   but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<JobCollection>,
            Resource.DefinitionWithTags<WithCreate>,
            WithJobCollectionQuota {
        }
    }


    /**
     * The template for a Job Collection update operation, containing all the settings that can be modified.
     */
    interface Update extends
        Appliable<JobCollection>,
        Resource.UpdateWithTags<Update>,
        UpdateStages.WithSku,
        UpdateStages.WithJobCollectionQuota,
        UpdateStages.WithState {
    }

    /**
     * Grouping of all the Job Collection update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the Job Collection update allowing to modify the SKU used.
         */
        interface WithSku {
            /**
             * Specifies the SKU type of the Job Collection.
             *
             * @param sku the SKU type
             * @return the next stage of the definition
             */
            Update withSku(SkuDefinition sku);
        }

        /**
         * The stage of the Job Collection update allowing to modify quota.
         */
        interface WithJobCollectionQuota {
            /**
             * Specifies the quota for the Job Collection.
             *
             * @param quota the quota object; if null, set the quota values to default for the current SKU
             * @return the next stage of the definition
             */
            Update withJobCollectionQuota(JobCollectionQuota quota);

            /**
             * Specifies the SKU type for the availability set.
             *
             * @param maxJobCount the maximum job count
             * @param maxJobOccurrence the maximum job occurrence
             * @param recurrenceFrequency the frequency of recurrence
             * @param retriesInterval the interval between retries
             * @return the next stage of the definition
             */
            Update withJobCollectionQuota(int maxJobCount, int maxJobOccurrence, RecurrenceFrequency recurrenceFrequency, int retriesInterval);
        }

        /**
         * The stage of the Job Collection update allowing to modify the state.
         */
        interface WithState {
            /**
             * Specifies the state of the Job Collection.
             *
             * @param state the job collection state
             * @return the next stage of the definition
             */
            Update withState(JobCollectionState state);
        }
    }
}
