/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.AvailabilitySetInner;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

/**
 * An immutable client-side representation of an Azure availability set.
 */
@Fluent()
public interface AvailabilitySet extends
        GroupableResource<ComputeManager>,
        Refreshable<AvailabilitySet>,
        Wrapper<AvailabilitySetInner>,
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
     * @return the availability set sku
     */
    AvailabilitySetSkuTypes sku();

    /**
     * @return the resource IDs of the virtual machines in the availability set
     */
    List<String> virtualMachineIds();

    /**
     * @return the statuses of the existing virtual machines in the availability set
     */
    List<InstanceViewStatus> statuses();

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
             * Specifies the sku type for the availability set.
             *
             * @param skuType the sku type
             * @return the next stage of the definition
             */
            WithCreate withSku(AvailabilitySetSkuTypes skuType);
        }

        /**
         * The stage of an availability set definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
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
         * The stage of the availability set definition allowing to specify sku.
         */
        interface WithSku {
            /**
             * Specifies the sku type for the availability set.
             *
             * @param skuType the sku type
             * @return the next stage of the definition
             */
            Update withSku(AvailabilitySetSkuTypes skuType);
        }
    }
    /**
     * The template for an availability set update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<AvailabilitySet>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithSku {
    }
}
