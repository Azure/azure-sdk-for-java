/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.Set;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.network.implementation.SubnetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * A client-side representation of a subnet of a virtual network.
 */
@Fluent()
public interface Subnet extends
    HasInner<SubnetInner>,
    ChildResource<Network> {

    /**
     * @return network interface IP configurations that are associated with this subnet
     * <p>
     * Note that this call may result in multiple calls to Azure to fetch all the referenced interfaces each time it is invoked.
     * <p>
     * @deprecated Use {@link Subnet#listNetworkInterfaceIPConfigurations()} instead.
     */
    @Method
    @Deprecated
    Set<NicIPConfiguration> getNetworkInterfaceIPConfigurations();

    /**
     * @return network interface IP configurations that are associated with this subnet
     * <p>
     * Note that this call may result in multiple calls to Azure to fetch all the referenced interfaces each time it is invoked.
     */
    @Method
    Set<NicIPConfiguration> listNetworkInterfaceIPConfigurations();

    /**
     * @return available private IP addresses within this network
     */
    @Beta(SinceVersion.V1_3_0)
    Set<String> listAvailablePrivateIPAddresses();

    /**
     * @return number of network interface IP configurations associated with this subnet
     */
    int networkInterfaceIPConfigurationCount();

    /**
     * @return the address space prefix, in CIDR notation, assigned to this subnet
     */
    String addressPrefix();

    /**
     * @return the network security group associated with this subnet, if any
     * <p>
     * Note that this method will result in a call to Azure each time it is invoked.
     */
    NetworkSecurityGroup getNetworkSecurityGroup();

    /**
     * @return the resource ID of the network security group associated with this subnet, if any
     */
    String networkSecurityGroupId();

    /**
     * @return the route table associated with this subnet, if any
     * <p>
     * Note that this method will result in a call to Azure each time it is invoked.
     */
    RouteTable getRouteTable();

    /**
     * @return the resource ID of the route table associated with this subnet, if any
     */
    String routeTableId();

    /**
     * Grouping of subnet definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the subnet definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAddressPrefix<ParentT> {
        }

        /**
         * The stage of the subnet definition allowing to specify the address space for the subnet.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAddressPrefix<ParentT> {
            /**
             * Specifies the IP address space of the subnet, within the address space of the network.
             * @param cidr the IP address space prefix using the CIDR notation
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAddressPrefix(String cidr);
        }

        /**
         * The stage of the subnet definition allowing to specify the network security group to assign to the subnet.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
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

        /**
         * The stage of a subnet definition allowing to specify a route table to associate with the subnet.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRouteTable<ParentT> {
            /**
             * Specifies an existing route table to associate with the subnet.
             * @param routeTable an existing route table to associate
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingRouteTable(RouteTable routeTable);

            /**
             * Specifies an existing route table to associate with the subnet.
             * @param resourceId the resource ID of an existing route table
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingRouteTable(String resourceId);
        }

        /** The final stage of the subnet definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the subnet definition
         * can be attached to the parent virtual network definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT>,
            WithNetworkSecurityGroup<ParentT>,
            WithRouteTable<ParentT> {
        }
    }

    /** The entirety of a Subnet definition.
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
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

            /**
             * Removes the association of this subnet with any network security group.
             * @return the next stage of the update
             */
            Update withoutNetworkSecurityGroup();
        }

        /**
         * The stage of a subnet update allowing to specify a route table to associate with the subnet, or remove an existing association.
         */
        interface WithRouteTable {
            /**
             * Specifies an existing route table to associate with the subnet.
             * @param routeTable an existing route table to associate
             * @return the next stage of the update
             */
            Update withExistingRouteTable(RouteTable routeTable);

            /**
             * Specifies an existing route table to associate with the subnet.
             * @param resourceId the resource ID of an existing route table
             * @return the next stage of the update
             */
            Update withExistingRouteTable(String resourceId);

            /**
             * Removes the association with a route table, if any.
             * @return the next stage of the update
             */
            Update withoutRouteTable();
        }

    }

    /**
     * The entirety of a subnet update as part of a network update.
     */
    interface Update extends
        UpdateStages.WithAddressPrefix,
        UpdateStages.WithNetworkSecurityGroup,
        UpdateStages.WithRouteTable,
        Settable<Network.Update> {
    }

    /**
     * Grouping of subnet definition stages applicable as part of a virtual network update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the subnet definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAddressPrefix<ParentT> {
        }

        /**
         * The stage of the subnet definition allowing to specify the address space for the subnet.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAddressPrefix<ParentT> {
            /**
             * Specifies the IP address space of the subnet, within the address space of the network.
             * @param cidr the IP address space prefix using the CIDR notation
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAddressPrefix(String cidr);
        }

        /**
         * The stage of the subnet definition allowing to specify the network security group to assign to the subnet.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
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

        /**
         * The stage of a subnet definition allowing to specify a route table to associate with the subnet.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRouteTable<ParentT> {
            /**
             * Specifies an existing route table to associate with the subnet.
             * @param routeTable an existing route table to associate
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingRouteTable(RouteTable routeTable);

            /**
             * Specifies an existing route table to associate with the subnet.
             * @param resourceId the resource ID of an existing route table
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingRouteTable(String resourceId);
        }

        /** The final stage of the subnet definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the subnet definition
         * can be attached to the parent virtual network definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT>,
            WithNetworkSecurityGroup<ParentT>,
            WithRouteTable<ParentT> {
        }
    }

    /** The entirety of a subnet definition as part of a virtual network update.
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT> extends
       UpdateDefinitionStages.Blank<ParentT>,
       UpdateDefinitionStages.WithAddressPrefix<ParentT>,
       UpdateDefinitionStages.WithNetworkSecurityGroup<ParentT>,
       UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
