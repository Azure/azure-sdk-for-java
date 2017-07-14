/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler;

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

    /***********************************************************
     * Actions
     ***********************************************************/


    /**
     * The entirety of the Job Collection definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
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
             * Specifies the SKU type for the availability set.
             *
             * @param skuDefinition the SKU type
             * @return the next stage of the definition
             */
            WithCreate withSku(SkuDefinition skuDefinition);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         *   but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<JobCollection>,
            Resource.DefinitionWithTags<WithCreate>,
            WithSku {
        }
    }


    /**
     * The template for a Job Collection update operation, containing all the settings that can be modified.
     */
    interface Update extends
        Appliable<JobCollection>,
        Resource.UpdateWithTags<Update> {
    }

    /**
     * Grouping of all the Search service update stages.
     */
    interface UpdateStages {
    }
}
