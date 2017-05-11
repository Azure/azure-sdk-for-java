/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.Set;

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

    /**
     * @return address spaces
     */
    Set<String> addressSpaces();

    /**
     * @return DHCP options
     */
    DhcpOptions dhcpOptions();

    /**
     * The entirety of the virtual network definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithAddressSpace,
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
            extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithAddressSpace> {
        }

        /**
         * The stage of the New Model blah definition allowing to specify the address space.
         */
        interface WithAddressSpace {
            /**
             * Specifies the address space.
             * @param cidr address space expressed using the CIDR notation
             * @return the next stage of the definition
             */
            WithCreate withAddressSpace(String cidr);

            /**
             * Specifies address spaces.
             * @param cidrs address spaces expressed using the CIDR notation
             * @return the next stage of the definition
             */
            WithCreate withAddressSpaces(String...cidrs);
        }

        /**
         * The stage of the New Model blah definition allowing to specify the DHCP options.
         */
        interface WithDhcpOptions {
            /**
             * Specifies DHCP options.
             * @param options DHCP options
             * @return the next stage of the definition
             */
            WithCreate withDhcpOptions(DhcpOptions options);
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
            DefinitionStages.WithAddressSpace,
            DefinitionStages.WithDhcpOptions,
            Creatable<NewTopLevelModel>,
            Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of virtual network update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the New Model blah update allowing to modify the address space.
         */
        interface WithAddressSpace {
            /**
             * Specifies the address space.
             * @param cidr address space expressed using the CIDR notation
             * @return the next stage of the update
             */
            Update withAddressSpace(String cidr);

            /**
             * Removes the specified address space.
             * @param cidr the address space to remove, expressed in the CIDR notation
             * @return the next stage of the update
             */
            Update withoutAddressSpace(String cidr);

            /**
             * Specifies address spaces.
             * @param cidrs address spaces expressed using the CIDR notation
             * @return the next stage of the update
             */
            Update withAddressSpaces(String...cidrs);
        }

        /**
         * The stage of the New Model blah update allowing to modify the DHCP options.
         */
        interface WithDhcpOptions {
            /**
             * Specifies DHCP options.
             * @param options DHCP options
             * @return the next stage of the definition
             */
            Update withDhcpOptions(DhcpOptions options);
        }

    }

    /**
     * The template for a virtual network update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<NewTopLevelModel>,
        UpdateStages.WithAddressSpace,
        UpdateStages.WithDhcpOptions,
        Resource.UpdateWithTags<Update> {
    }
}
