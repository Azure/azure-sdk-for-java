/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.SubnetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of a subnet of a virtual network.
 */
@Fluent()
public interface Subnet extends
    Wrapper<SubnetInner>,
    ChildResource<Network> {

    /**
     * @return the address space prefix, in CIDR notation, assigned to this subnet
     */
    String addressPrefix();

    /**
     * @return the network security group associated with this subnet
     * <p>
     * Note that this method will result in a call to Azure each time it is invoked.
     */
    NetworkSecurityGroup getNetworkSecurityGroup();

    /**
     * Grouping of subnet definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the subnet definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithAddressPrefix<ParentT> {
        }

        /**
         * The stage of the subnet definition allowing to specify the address space for the subnet.
         * @param <ParentT> the parent type
         */
        interface WithAddressPrefix<ParentT> {
            /**
             * Specifies the IP address space of the subnet, within the address space of the network.
             * @param cidr the IP address space prefix using the CIDR notation
             * @return the next stage of the subnet definition
             */
            WithAttach<ParentT> withAddressPrefix(String cidr);
        }

        /**
         * The stage of the subnet definition allowing to specify the network security group to assign to the subnet.
         * @param <ParentT> the parent type
         */
        interface WithNetworkSecurityGroup<ParentT> {
            /**
             * Assigns an existing network security group to this subnet.
             * @param resourceId the resource ID of the network security group
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingNetworkSecurityGroup(String resourceId);

            /**
             * Assigns an existing network security group to this subnet.
             * @param nsg the network security group to assign
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingNetworkSecurityGroup(NetworkSecurityGroup nsg);
        }

        /** The final stage of the subnet definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the subnet definition
         * can be attached to the parent virtual network definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT>,
            WithNetworkSecurityGroup<ParentT> {
        }
    }

    /** The entirety of a Subnet definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAddressPrefix<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of subnet update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the subnet update allowing to change the address space for the subnet.
         */
        interface WithAddressPrefix {
            /**
             * Specifies the IP address space of the subnet, within the address space of the network.
             * @param cidr the IP address space prefix using the CIDR notation
             * @return the next stage
             */
            Update withAddressPrefix(String cidr);
        }

        /**
         * The stage of the subnet update allowing to change the network security group to assign to the subnet.
         */
        interface WithNetworkSecurityGroup {
            /**
             * Assigns an existing network security group to this subnet.
             * @param resourceId the resource ID of the network security group
             * @return the next stage of the update
             */
            Update withExistingNetworkSecurityGroup(String resourceId);

            /**
             * Assigns an existing network security group to this subnet.
             * @param nsg the network security group to assign
             * @return the next stage of the update
             */
            Update withExistingNetworkSecurityGroup(NetworkSecurityGroup nsg);
        }
    }

    /**
     * The entirety of a subnet update as part of a network update.
     */
    interface Update extends
        UpdateStages.WithAddressPrefix,
        UpdateStages.WithNetworkSecurityGroup,
        Settable<Network.Update> {
    }

    /**
     * Grouping of subnet definition stages applicable as part of a virtual network update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the subnet definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithAddressPrefix<ParentT> {
        }

        /**
         * The stage of the subnet definition allowing to specify the address space for the subnet.
         * @param <ParentT> the parent type
         */
        interface WithAddressPrefix<ParentT> {
            /**
             * Specifies the IP address space of the subnet, within the address space of the network.
             * @param cidr the IP address space prefix using the CIDR notation
             * @return the next stage of the subnet definition
             */
            WithAttach<ParentT> withAddressPrefix(String cidr);
        }

        /**
         * The stage of the subnet definition allowing to specify the network security group to assign to the subnet.
         * @param <ParentT> the parent type
         */
        interface WithNetworkSecurityGroup<ParentT> {
            /**
             * Assigns an existing network security group to this subnet.
             * @param resourceId the resource ID of the network security group
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingNetworkSecurityGroup(String resourceId);

            /**
             * Assigns an existing network security group to this subnet.
             * @param nsg the network security group to assign
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingNetworkSecurityGroup(NetworkSecurityGroup nsg);
        }

        /** The final stage of the subnet definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the subnet definition
         * can be attached to the parent virtual network definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT>,
            WithNetworkSecurityGroup<ParentT> {
        }
    }

    /** The entirety of a subnet definition as part of a virtual network update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
       UpdateDefinitionStages.Blank<ParentT>,
       UpdateDefinitionStages.WithAddressPrefix<ParentT>,
       UpdateDefinitionStages.WithNetworkSecurityGroup<ParentT>,
       UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
