/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.network.implementation.api.NetworkInterfaceInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;

import java.io.IOException;
import java.util.List;

/**
 * Network interface.
 */
public interface NetworkInterface extends
        GroupableResource,
        Refreshable<NetworkInterface>,
        Wrapper<NetworkInterfaceInner>,
        Updatable<NetworkInterface.Update> {

    /**
     * @return <tt>true</tt> if IP forwarding is enabled in this network interface
     */
    Boolean isIpForwardingEnabled();

    /**
     * @return <tt>true</tt> if this is primary network interface in a virtual machine
     */
    Boolean isPrimary();

    /**
     * @return the MAC Address of the network interface
     */
    String macAddress();

    /**
     *
     * @return the Internal DNS name assigned to this network interface
     */
    String internalDnsNameLabel();

    /**
     * Gets the fully qualified domain name of this network interface.
     * <p>
     * a network interface receives FQDN as a part of assigning it to a virtual machine
     *
     * @return the qualified domain name
     */
    String internalFqdn();

    /**
     * @return IP addresses this network interface's DNS servers
     */
    List<String> dnsServers();

    /**
     * Gets the resource id of the public IP address associated with this network interface.
     * <p>
     * returns null if there is no public IP associated
     *
     * @return public ip resource id
     */
    String publicIpAddressId();

    /**
     * Gets the public IP address associated with this network interface.
     * <p>
     * note that this method makes a rest API call to fetch the public IP
     *
     * @return the public IP associated with this network interface
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    PublicIpAddress publicIpAddress() throws CloudException, IOException;

    /**
     * @return the resource id of the virtual network subnet associated with this network interface.
     */
    String subnetId();

    /**
     * Gets the virtual network associated with this network interface.
     *
     * @return the virtual network associated with this network interface.
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    Network network() throws CloudException, IOException;

    /**
     * Gets the private IP address allocated to this network interface.
     * <p>
     * the private IP will be within the virtual network subnet of this network interface.
     *
     * @return the private IP addresses
     */
    String privateIp();

    /**
     * @return the private IP allocation method (Dynamic, Static)
     */
    String privateIpAllocationMethod();

    /**
     * Container interface for all the definitions.
     */
    interface Definitions extends
            NetworkInterface.DefinitionBlank,
            NetworkInterface.DefinitionWithGroup,
            NetworkInterface.DefinitionWithNetwork,
            NetworkInterface.DefinitionWithSubnet,
            NetworkInterface.DefinitionWithPrivateIp,
            NetworkInterface.DefinitionWithPublicIpAddress,
            NetworkInterface.DefinitionCreatable {
    }

    /**
     * The first stage of the network interface.
     */
    interface DefinitionBlank
            extends GroupableResource.DefinitionWithRegion<NetworkInterface.DefinitionWithGroup> {
    }

    /**
     * The stage of the network interface definition allowing to specify the resource group.
     */
    interface DefinitionWithGroup
            extends GroupableResource.DefinitionWithGroup<NetworkInterface.DefinitionWithNetwork> {
    }

    /**
     * The stage of the network interface definition allowing to specify the virtual network.
     */
    interface DefinitionWithNetwork {
        /**
         * Create a new virtual network to associate the network interface with, based on the provided definition.
         *
         * @param creatable a creatable definition for a new virtual network
         * @return the next stage of the network interface definition
         */
        DefinitionWithPrivateIp<DefinitionWithPublicIpAddress> withNewNetwork(Network.DefinitionCreatable creatable);

        /**
         * Creates a new virtual network to associate with the network interface.
         * <p>
         * the virtual network will be created in the same resource group and region as of network interface,
         * it will be created with the specified address space and a default subnet covering the entirety of
         * the network IP address space.
         *
         * @param name the name of the new virtual network
         * @param addressSpace the address space for rhe virtual network
         * @return the next stage of the network interface definition
         */
        DefinitionWithPrivateIp<DefinitionWithPublicIpAddress> withNewNetwork(String name, String addressSpace);

        /**
         * Creates a new virtual network to associate with the network interface.
         * <p>
         * the virtual network will be created in the same resource group and region as of network interface,
         * it will be created with the specified address space and a default subnet covering the entirety of
         * the network IP address space.
         *
         * @param addressSpace the address space for the virtual network
         * @return the next stage of the network interface definition
         */
        DefinitionWithPrivateIp<DefinitionWithPublicIpAddress> withNewNetwork(String addressSpace);

        /**
         *
         * @param network an existing virtual network
         * @return the next stage of the network interface definition
         */
        DefinitionWithSubnet<DefinitionWithPrivateIp> withExistingNetwork(Network network);
    }

    /**
     * The stage of the network interface definition allowing to specify subnet.
     *
     * @param <T> the next stage after setting the subnet
     */
    interface DefinitionWithSubnet<T> {
        /**
         * Associate a subnet with the network interface.
         *
         * @param name the subnet name
         * @return the next stage of the network interface definition
         */
        T withSubnet(String name);
    }

    /**
     * The stage of the network interface definition allowing to specify private IP address within
     * a virtual network subnet.
     *
     * @param <T> the next stage after specifying the private IP configuration
     */
    interface DefinitionWithPrivateIp<T> {
        /**
         * Enables dynamic private IP address allocation within the specified existing virtual network
         * subnet for the network interface.
         *
         * @return the next stage of network interface definition
         */
        T withPrivateIpAddressDynamic();

        /**
         * Assigns the specified static private IP address within the specified existing virtual network
         * subnet to the network interface.
         *
         * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
         *                               the network interface
         * @return the next stage of network interface definition
         */
        T withPrivateIpAddressStatic(String staticPrivateIpAddress);
    }

    /**
     * The stage of the network interface definition allowing to associate it with a public IP address.
     */
    interface DefinitionWithPublicIpAddress extends
            DefinitionCreatable {
        /**
         * Create a new public IP address to associate the network interface with, based on the provided definition.
         *
         * @param creatable a creatable definition for a new public IP
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withNewPublicIpAddress(PublicIpAddress.DefinitionCreatable creatable);

        /**
         * Creates a new public IP address in the same region and group as the resource and associate it
         * with the network interface.
         * <p>
         * the internal name and DNS label for the public IP address will be derived from the network interface name
         *
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withNewPublicIpAddress();

        /**
         * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
         * and associate it with the network interface.
         * <p>
         * the internal name for the public IP address will be derived from the DNS label
         *
         * @param leafDnsLabel the leaf domain label
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withNewPublicIpAddress(String leafDnsLabel);

        /**
         * Associates an existing public IP address with the network interface.
         *
         * @param publicIpAddress an existing public IP address
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withExistingPublicIpAddress(PublicIpAddress publicIpAddress);
    }

    /**
     * The stage of the network interface definition which contains all the minimum required inputs for
     * the resource to be created (via {@link DefinitionCreatable#create()}), but also allows
     * for any other optional settings to be specified.
     */
    interface DefinitionCreatable extends
            Creatable<NetworkInterface>,
            Resource.DefinitionWithTags<NetworkInterface.DefinitionCreatable> {
        /**
         * Enable IP forwarding in the network interface.
         *
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withIpForwardingEnabled();

        /**
         * Specifies the IP address of the custom DNS server to associate with the network interface.
         * <p>
         * Note this method's effect is additive, i.e. each time it is used, the new dns server is
         * added to the network interface.
         *
         * @param ipAddress the IP address of the DNS server
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withDnsServer(String ipAddress);

        /**
         * Specifies the internal DNS name label for the network interface.
         *
         * @param dnsNameLabel the internal DNS name label
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withInternalDnsNameLabel(String dnsNameLabel);
    }

    /**
     * The template for a network interface update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<NetworkInterface>,
            Resource.UpdateWithTags<Update> {
        /**
         * Associate a subnet with the network interface.
         *
         * @param name the subnet name
         * @return the next stage of the network interface update
         */
        Update withSubnet(String name);

        /**
         * Enables dynamic private IP address allocation within the specified existing virtual network
         * subnet for the network interface.
         *
         * @return the next stage of network interface update
         */
        Update withPrivateIpAddressDynamic();

        /**
         * Assigns the specified static private IP address within the specified existing virtual network
         * subnet to the network interface.
         *
         * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
         *                               the network interface
         * @return the next stage of network interface update
         */
        Update withPrivateIpAddressStatic(String staticPrivateIpAddress);

        /**
         * Enable IP forwarding in the network interface.
         *
         * @return the next stage of the network interface update
         */
        Update withIpForwardingEnabled();

        /**
         * Disable IP forwarding in the network interface.
         *
         * @return the next stage of the network interface update
         */
        Update withIpForwardingDisabled();

        /**
         * Specifies the IP address of the custom DNS server to associate with the network interface.
         * <p>
         * Note this method's effect is additive, i.e. each time it is used, the new dns server is
         * added to the network interface.
         *
         * @param ipAddress the IP address of the DNS server
         * @return the next stage of the network interface update
         */
        Update withDnsServer(String ipAddress);

        /**
         * Removes a DNS server associated with the network interface.
         *
         * @param ipAddress the IP address of the DNS server
         * @return the next stage of the network interface update
         */
        Update withoutDnsServer(String ipAddress);

        /**
         * Specifies to use the default Azure DNS server for the network interface.
         * <p>
         * Using azure DNS server will remove any custom DNS server associated with this network interface.
         *
         * @return the next stage of the network interface update
         */
        Update withAzureDnsServer();

        /**
         * Create a new public IP address to associate the network interface with, based on the provided definition.
         * <p>
         * if there is an existing public IP association then that will be removed in favour of this
         *
         * @param creatable a creatable definition for a new public IP
         * @return the next stage of the network interface update
         */
        Update withNewPublicIpAddress(PublicIpAddress.DefinitionCreatable creatable);

        /**
         * Creates a new public IP address in the same region and group as the resource and associate it
         * with the network interface.
         * <p>
         * the internal name and DNS label for the public IP address will be derived from the network interface name,
         * if there is an existing public IP association then that will be removed in favour of this
         *
         * @return the next stage of the network interface update
         */
        Update withNewPublicIpAddress();

        /**
         * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
         * and associate it with the network interface.
         * <p>
         * the internal name for the public IP address will be derived from the DNS label, if there is an existing
         * public IP association then that will be removed in favour of this
         *
         * @param leafDnsLabel the leaf domain label
         * @return the next stage of the network interface update
         */
        Update withNewPublicIpAddress(String leafDnsLabel);

        /**
         * Specifies that remove any public IP associated with the network interface.
         *
         * @return the next stage of the network interface update
         */
        Update withoutPublicIpAddress();

        /**
         * Associates an existing public IP address with the network interface. if there is an existing
         * public IP association then that will be removed in favour of this
         *
         * @param publicIpAddress an existing public IP address
         * @return the next stage of the network interface update
         */
        Update withExistingPublicIpAddress(PublicIpAddress publicIpAddress);
    }
}
