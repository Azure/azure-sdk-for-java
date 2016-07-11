/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.network.implementation.VirtualNetworkInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * Entry point for Virtual Network management API in Azure.
 */
public interface Network extends
        GroupableResource,
        Refreshable<Network>,
        Wrapper<VirtualNetworkInner>,
        Updatable<Network.Update> {

    /***********************************************************
     * Getters
     ***********************************************************/
    /**
     * @return list of address spaces associated with this virtual network, in the CIDR notation
     */
    List<String> addressSpaces();

    /**
     * @return list of DNS server IP addresses associated with this virtual network
     */
    List<String> dnsServerIPs();

    /**
     * @return subnets of this virtual network as a map indexed by subnet name
     *
     * <p>Note that when a virtual network is created with no subnets explicitly defined, a default subnet is
     * automatically created with the name "subnet1".
     */
    Map<String, Subnet> subnets();

    /**
     * The entirety of the virtual network definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithSubnet,
        DefinitionStages.WithCreate,
        DefinitionStages.WithCreateAndSubnet {
    }

    /**
     * Grouping of virtual network definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a virtual network definition.
         */
        interface Blank
            extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the virtual network definition allowing to specify the resource group.
         */
        interface WithGroup
            extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithCreate> {
        }

        /**
         * The stage of the virtual network definition allowing to add subnets.
         */
        interface WithSubnet {
            /**
             * Explicitly adds a subnet to the virtual network.
             * <p>
             * If no subnets are explicitly specified, a default subnet called "subnet1" covering the
             * entire first address space will be created.
             * <p>
             * Note this method's effect is additive, i.e. each time it is used, a new subnet is added to the network.
             * @param name the name to assign to the subnet
             * @param cidr the address space of the subnet, within the address space of the network, using the CIDR notation
             * @return the next stage of the virtual network definition
             */
            DefinitionStages.WithCreateAndSubnet withSubnet(String name, String cidr);

            /**
             * Explicitly defines subnets in the virtual network based on the provided map.
             * @param nameCidrPairs a {@link Map} of CIDR addresses for the subnets, indexed by the name of each subnet to be defined
             * @return the next stage of the virtual network definition
             */
            DefinitionStages.WithCreateAndSubnet withSubnets(Map<String, String> nameCidrPairs);

            /**
             * Begins the definition of a new subnet to add to the virtual network.
             * <p>
             * The definition must be completed with a call to {@link Subnet.DefinitionStages.WithAttach#attach()}
             * @param name the name of the subnet
             * @return the first stage of the new subnet definition
             */
            Subnet.DefinitionStages.Blank<WithCreateAndSubnet> defineSubnet(String name);
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
            Creatable<Network>,
            Resource.DefinitionWithTags<WithCreate> {

            /**
             * Specifies the IP address of an existing DNS server to associate with the virtual network.
             * <p>
             * Note this method's effect is additive, i.e. each time it is used, a new dns server is added
             * to the network.
             * @param ipAddress the IP address of the DNS server
             * @return the next stage of the virtual network definition
             */
            WithCreate withDnsServer(String ipAddress);

            /**
             * Explicitly adds an address space to the virtual network.
             * <p>
             * If no address spaces are explicitly specified, a default address space with the CIDR "10.0.0.0/16" will be
             * assigned to the virtual network.
             * <p>
             * Note that this method's effect is additive, i.e. each time it is used, a new address space is added to the network.
             * This method does not check for conflicts or overlaps with other address spaces. If there is a conflict,
             * a cloud exception may be thrown at the time the network is created.
             * @param cidr the CIDR representation of the address space
             * @return the next stage of the virtual network definition
             */
            WithCreateAndSubnet withAddressSpace(String cidr);
        }

        /**
         * The stage of the public IP definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified, including adding subnets.
         */
        interface WithCreateAndSubnet extends
            DefinitionStages.WithCreate,
            DefinitionStages.WithSubnet {
        }
    }

    /**
     * Grouping of virtual network update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the virtual network update allowing to add or remove subnets.
         */
        interface WithSubnet {
            /**
             * Explicitly adds a subnet to the virtual network.
             * <p>
             * Note this method's effect is additive, i.e. each time it is used, a new subnet is added to the network.
             * @param name the name to assign to the subnet
             * @param cidr the address space of the subnet, within the address space of the network, using the CIDR notation
             * @return the next stage of the virtual network update
             */
            Update withSubnet(String name, String cidr);

            /**
             * Explicitly defines all the subnets in the virtual network based on the provided map.
             * <p>
             * This replaces any previously existing subnets.
             * @param nameCidrPairs a {@link Map} of CIDR addresses for the subnets, indexed by the name of each subnet to be added
             * @return the next stage of the virtual network update
             */
            Update withSubnets(Map<String, String> nameCidrPairs);

            /**
             * Removes a subnet from the virtual network.
             * @param name name of the subnet to remove
             * @return the next stage of the virtual network update
             */
            Update withoutSubnet(String name);

            /**
             * Begins the description of an update of an existing subnet of this network.
             * @param name the name of an existing subnet
             * @return the first stage of the subnet update description
             */
            Subnet.Update updateSubnet(String name);

            /**
             * Begins the definition of a new subnet to be added to this virtual network.
             * @param name the name of the new subnet
             * @return the first stage of the new subnet definition
             */
            Subnet.UpdateDefinitionStages.Blank<Update> defineSubnet(String name);
        }

        /**
         * The stage of the virtual network update allowing to specify the DNS server.
         */
        interface WithDnsServer {
            /**
             * Specifies the IP address of the DNS server to associate with the virtual network.
             * <p>
             * Note this method's effect is additive, i.e. each time it is used, a new DNS server is
             * added to the network
             * @param ipAddress the IP address of the DNS server
             * @return the next stage of the virtual network update
             */
            Update withDnsServer(String ipAddress);
        }

        /**
         * The stage of the virtual network update allowing to specify the address space.
         */
        interface WithAddressSpace {
            /**
             * Explicitly adds an address space to the virtual network.
             * <p>
             * Note this method's effect is additive, i.e. each time it is used, a new address space is added to the network.
             * <p>
             * This method does not check for conflicts or overlaps with other address spaces. If there is a conflict,
             * a cloud exception may be thrown after the update is applied.
             * @param cidr the CIDR representation of the address space
             * @return the next stage of the virtual network update
             */
            Update withAddressSpace(String cidr);
        }
    }

    /**
     * The template for a virtual network update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<Network>,
        Resource.UpdateWithTags<Update>,
        UpdateStages.WithSubnet,
        UpdateStages.WithDnsServer,
        UpdateStages.WithAddressSpace {
    }
}
