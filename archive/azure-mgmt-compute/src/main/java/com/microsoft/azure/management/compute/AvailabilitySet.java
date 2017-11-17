/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.AvailabilitySetInner;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

import java.util.List;
import java.util.Set;

/**
 * An immutable client-side representation of an Azure availability set.
 */
@Fluent()
public interface AvailabilitySet extends
        GroupableResource<ComputeManager, AvailabilitySetInner>,
        Refreshable<AvailabilitySet>,
        Updatable<AvailabilitySet.Update> {

    /**
     * @return the update domain count of this availability set
     */
    int updateDomainCount();

    /**
     * @return the fault domain count of this availability set
     */
    int faultDomainCount();

    /**
     * @return the availability set SKU
     */
    AvailabilitySetSkuTypes sku();

    /**
     * @return the resource IDs of the virtual machines in the availability set
     */
    Set<String> virtualMachineIds();

    /**
     * @return the statuses of the existing virtual machines in the availability set
     */
    List<InstanceViewStatus> statuses();

    /**
     * @return the virtual machine sizes supported in the availability set
     */
    PagedList<VirtualMachineSize> listVirtualMachineSizes();


    // Fluent interfaces

    /**
     * Container interface for all the definitions related to an availability set.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of availability set definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an availability set definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the availability set definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of the availability set definition allowing to specify the update domain count.
         */
        interface WithUpdateDomainCount {
            /**
             * Specifies the update domain count for the availability set.
             * @param updateDomainCount update domain count
             * @return the next stage of the definition
             */
            WithCreate withUpdateDomainCount(int updateDomainCount);
        }

        /**
         * The stage of the availability set definition allowing to specify the fault domain count.
         */
        interface WithFaultDomainCount {
            /**
             * Specifies the fault domain count for the availability set.
             * @param faultDomainCount the fault domain count
             * @return the next stage of the definition
             */
            WithCreate withFaultDomainCount(int faultDomainCount);
        }

        /**
         * The stage of the availability set definition allowing enable or disable for managed disk.
         */
        interface WithSku {
            /**
             * Specifies the SKU type for the availability set.
             *
             * @param skuType the sku type
             * @return the next stage of the definition
             */
            WithCreate withSku(AvailabilitySetSkuTypes skuType);
        }

        /**
         * The stage of an availability set definition which contains all the minimum required inputs for
         * the resource to be created but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<AvailabilitySet>,
                Resource.DefinitionWithTags<WithCreate>,
                WithUpdateDomainCount,
                WithFaultDomainCount,
                WithSku {
        }
    }

    /**
     * Grouping of availability set update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the availability set definition allowing to specify SKU.
         */
        interface WithSku {
            /**
             * Specifies the SKU type for the availability set.
             *
             * @param skuType the SKU type
             * @return the next stage of the definition
             */
            Update withSku(AvailabilitySetSkuTypes skuType);
        }
    }
    /**
     * The template for an availability set update operation, containing all the settings that
     * can be modified.
     */
    interface Update extends
            Appliable<AvailabilitySet>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithSku {
    }
}
