/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.google.common.annotations.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.network.implementation.VirtualNetworkInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * Entry point for Parent management API in Azure.
 */
@Fluent()
@Beta()
public interface NewTopLevelModel extends
        GroupableResource<NetworkManager, VirtualNetworkInner>,
        Refreshable<NewTopLevelModel>,
        Updatable<NewTopLevelModel.Update> {

    /***********************************************************
     * Getters
     ***********************************************************/

    /**
     * The entirety of the virtual network definition.
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
         * The first stage of a foo definition.
         */
        interface Blank
            extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the foo definition allowing to specify the resource group.
         */
        interface WithGroup
            extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithCreate> {
        }

        /**
         * The stage of the virtual network definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified, except for adding subnets.
         * <p>
         * Subnets can be added only right after the address space is explicitly specified
         * (see {@link WithCreate#withAddressSpace(String)}).
         */
        interface WithCreate extends
            Creatable<NewTopLevelModel>,
            Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of virtual network update stages.
     */
    interface UpdateStages {

    }

    /**
     * The template for a virtual network update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<NewTopLevelModel>,
        Resource.UpdateWithTags<Update> {
    }
}
