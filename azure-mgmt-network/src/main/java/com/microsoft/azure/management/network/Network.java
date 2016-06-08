/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.network.implementation.api.VirtualNetworkInner;
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
     * @return list of subnets of this virtual network
     */
    List<Subnet> subnets();

    /**************************************************************
     * Fluent interfaces for provisioning
     **************************************************************/

    /**
     * The entirety of the virtual network definition.
     */
    interface Definitions extends
        DefinitionBlank,
        DefinitionWithGroup,
        DefinitionWithSubnet,
        DefinitionCreatable,
        DefinitionCreatableWithSubnet {
    }

    /**
     * The first stage of a virtual network definition.
     */
    interface DefinitionBlank
        extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    /**
     * The stage of the virtual network definition allowing to specify the resource group.
     */
    interface DefinitionWithGroup
        extends GroupableResource.DefinitionWithGroup<DefinitionCreatable> {
    }

    /**
     * The stage of the virtual network definition allowing to add subnets.
     */
    interface DefinitionWithSubnet {
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
        DefinitionCreatableWithSubnet withSubnet(String name, String cidr);

        /**
         * Explicitly defines subnets in the virtual network based on the provided map.
         * @param nameCidrPairs a {@link Map} of CIDR addresses for the subnets, indexed by the name of each subnet to be defined
         * @return the next stage of the virtual network definition
         */
        DefinitionCreatableWithSubnet withSubnets(Map<String, String> nameCidrPairs);

        Subnet.DefinitionBlank<DefinitionCreatableWithSubnet> defineSubnet(String name);
    }


    /**
     * The stage of the virtual network update allowing to add or remove subnets.
     */
    interface UpdateWithSubnet {
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
    }

    /**
     * The stage of the virtual network definition which contains all the minimum required inputs for
     * the resource to be created (via {@link DefinitionCreatable#create()}), but also allows
     * for any other optional settings to be specified, except for adding subnets.
     * <p>
     * Subnets can be added only right after the address space is explicitly specified
     * (see {@link DefinitionWithAddressSpace#withAddressSpace(String)).
     */
    interface DefinitionCreatable extends
        Creatable<Network>,
        Resource.DefinitionWithTags<DefinitionCreatable> {

        /**
         * Specifies the IP address of an existing DNS server to associate with the virtual network.
         * <p>
         * Note this method's effect is additive, i.e. each time it is used, a new dns server is added
         * to the network.
         * @param ipAddress the IP address of the DNS server
         * @return the next stage of the virtual network definition
         */
        DefinitionCreatable withDnsServer(String ipAddress);

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
        DefinitionCreatableWithSubnet withAddressSpace(String cidr);
    }

    /**
     * The stage of the public IP definition which contains all the minimum required inputs for
     * the resource to be created (via {@link DefinitionCreatable#create()}), but also allows
     * for any other optional settings to be specified, including adding subnets.
     */
    interface DefinitionCreatableWithSubnet extends
        DefinitionCreatable,
        DefinitionWithSubnet {
    }

    /**
     * The template for a public IP address update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<Network>,
        Resource.UpdateWithTags<Update>,
        UpdateWithSubnet {

        /**
         * Specifies the IP address of the DNS server to associate with the virtual network.
         * <p>
         * Note this method's effect is additive, i.e. each time it is used, a new dns server is
         * added to the network
         * @param ipAddress the IP address of the DNS server
         * @return the next stage of the virtual network update
         */
        Update withDnsServer(String ipAddress);

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
