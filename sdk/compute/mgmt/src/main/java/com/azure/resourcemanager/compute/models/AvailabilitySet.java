// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.inner.AvailabilitySetInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.List;
import java.util.Set;

/** An immutable client-side representation of an Azure availability set. */
@Fluent
public interface AvailabilitySet
    extends GroupableResource<ComputeManager, AvailabilitySetInner>,
        Refreshable<AvailabilitySet>,
        Updatable<AvailabilitySet.Update> {

    /** @return the update domain count of this availability set */
    int updateDomainCount();

    /** @return the fault domain count of this availability set */
    int faultDomainCount();

    /** @return the availability set SKU */
    AvailabilitySetSkuTypes sku();

    /** @return the resource IDs of the virtual machines in the availability set */
    Set<String> virtualMachineIds();

    /**
     * Get specifies information about the proximity placement group that the virtual machine scale set should be
     * assigned to.
     *
     * @return the proximityPlacementGroup.
     */
    ProximityPlacementGroup proximityPlacementGroup();

    /** @return the statuses of the existing virtual machines in the availability set */
    List<InstanceViewStatus> statuses();

    /** @return the virtual machine sizes supported in the availability set */
    PagedIterable<VirtualMachineSize> listVirtualMachineSizes();

    // Fluent interfaces

    /** Container interface for all the definitions related to an availability set. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithGroup, DefinitionStages.WithCreate {
    }

    /** Grouping of availability set definition stages. */
    interface DefinitionStages {
        /** The first stage of an availability set definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the availability set definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /** The stage of the availability set definition allowing to specify the update domain count. */
        interface WithUpdateDomainCount {
            /**
             * Specifies the update domain count for the availability set.
             *
             * @param updateDomainCount update domain count
             * @return the next stage of the definition
             */
            WithCreate withUpdateDomainCount(int updateDomainCount);
        }

        /** The stage of the availability set definition allowing to specify the fault domain count. */
        interface WithFaultDomainCount {
            /**
             * Specifies the fault domain count for the availability set.
             *
             * @param faultDomainCount the fault domain count
             * @return the next stage of the definition
             */
            WithCreate withFaultDomainCount(int faultDomainCount);
        }

        /** The stage of the availability set definition allowing enable or disable for managed disk. */
        interface WithSku {
            /**
             * Specifies the SKU type for the availability set.
             *
             * @param skuType the sku type
             * @return the next stage of the definition
             */
            WithCreate withSku(AvailabilitySetSkuTypes skuType);
        }

        /** The stage of the availability set definition setting ProximityPlacementGroup. */
        interface WithProximityPlacementGroup {
            /**
             * Set information about the proximity placement group that the availability set should be assigned to.
             *
             * @param promixityPlacementGroupId The Id of the proximity placement group subResource.
             * @return the next stage of the definition.
             */
            WithCreate withProximityPlacementGroup(String promixityPlacementGroupId);

            /**
             * Creates a new proximity placement gruup witht he specified name and then adds it to the availability set.
             *
             * @param proximityPlacementGroupName The name of the group to be created.
             * @param type the type of the group
             * @return the next stage of the definition.
             */
            WithCreate withNewProximityPlacementGroup(
                String proximityPlacementGroupName, ProximityPlacementGroupType type);
        }

        /**
         * The stage of an availability set definition which contains all the minimum required inputs for the resource
         * to be created but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<AvailabilitySet>,
                Resource.DefinitionWithTags<WithCreate>,
                WithUpdateDomainCount,
                WithFaultDomainCount,
                WithSku,
                WithProximityPlacementGroup {
        }
    }

    /** Grouping of availability set update stages. */
    interface UpdateStages {
        /** The stage of the availability set definition allowing to specify SKU. */
        interface WithSku {
            /**
             * Specifies the SKU type for the availability set.
             *
             * @param skuType the SKU type
             * @return the next stage of the definition
             */
            Update withSku(AvailabilitySetSkuTypes skuType);
        }

        /** The stage of the availability set definition setting ProximityPlacementGroup. */
        interface WithProximityPlacementGroup {
            /**
             * Set information about the proximity placement group that the availability set should be assigned to.
             *
             * @param promixityPlacementGroupId The Id of the proximity placement group subResource.
             * @return the next stage of the definition.
             */
            Update withProximityPlacementGroup(String promixityPlacementGroupId);

            /**
             * Remove the proximity placement group from the availability set.
             *
             * @return the next stage of the definition.
             */
            Update withoutProximityPlacementGroup();
        }
    }
    /** The template for an availability set update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<AvailabilitySet>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithSku,
            UpdateStages.WithProximityPlacementGroup {
    }
}
