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
    boolean isIpForwardingEnabled();

    /**
     * @return <tt>true</tt> if this is primary network interface in a virtual machine
     */
    boolean isPrimary();

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
     * Gets the public IP address associated with this network interface.
     * <p>
     * note that this method makes a rest API call to fetch the public IP
     *
     * @return the public IP associated with this network interface
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    PublicIpAddress primaryPublicIpAddress() throws CloudException, IOException;

    /**
     * @return the resource id of the virtual network subnet associated with this network interface.
     * TODO: This will be removed once our types starts supporting lazy loading
     */
    String primarySubnetId();

    /**
     * Gets the virtual network associated this network interface's primary Ip configuration.
     * <p>
     * note that this method makes a rest API call to fetch the public IP
     *
     * @return the virtual network associated with this network interface.
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    Network primaryNetwork() throws CloudException, IOException;

    /**
     * Gets the private IP address allocated to this network interface's primary Ip configuration.
     * <p>
     * the private IP will be within the virtual network subnet of this network interface.
     *
     * @return the private IP addresses
     */
    String primaryPrivateIp();

    /**
     * @return the private IP allocation method (Dynamic, Static) of this network interface's primary Ip configuration.
     */
    String primaryPrivateIpAllocationMethod();

    /**
     * @return the Ip configurations of this network interface
     */
    List<NicIpConfiguration> ipConfigurations();

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
     * The stage of the network interface definition allowing to specify the virtual network for primary Ip configuration.
     */
    interface DefinitionWithNetwork {
        /**
         * Create a new virtual network to associate with the network interface's primary Ip configuration, based on
         * the provided definition.
         *
         * @param creatable a creatable definition for a new virtual network
         * @return the next stage of the network interface definition
         */
        DefinitionWithPrivateIp withNewPrimaryNetwork(Network.DefinitionCreatable creatable);

        /**
         * Creates a new virtual network to associate with the network interface's primary Ip configuration.
         * <p>
         * the virtual network will be created in the same resource group and region as of network interface,
         * it will be created with the specified address space and a default subnet covering the entirety of
         * the network IP address space.
         *
         * @param name the name of the new virtual network
         * @param addressSpace the address space for rhe virtual network
         * @return the next stage of the network interface definition
         */
        DefinitionWithPrivateIp withNewPrimaryNetwork(String name, String addressSpace);

        /**
         * Creates a new virtual network to associate with the network interface's primary Ip configuration.
         * <p>
         * the virtual network will be created in the same resource group and region as of network interface,
         * it will be created with the specified address space and a default subnet covering the entirety of
         * the network IP address space.
         *
         * @param addressSpace the address space for the virtual network
         * @return the next stage of the network interface definition
         */
        DefinitionWithPrivateIp withNewPrimaryNetwork(String addressSpace);

        /**
         * Associate an existing virtual network with the network interface's primary Ip configuration.
         *
         * @param network an existing virtual network
         * @return the next stage of the network interface definition
         */
        DefinitionWithSubnet withExistingPrimaryNetwork(Network network);
    }

    /**
     * The stage of the network interface definition allowing to specify subnet.
     *
     * @param <T> the next stage after setting the subnet
     */
    interface DefinitionWithSubnet {
        /**
         * Associate a subnet with the network interface's primary Ip configuration.
         *
         * @param name the subnet name
         * @return the next stage of the network interface definition
         */
        DefinitionWithPrivateIp withSubnet(String name);
    }

    /**
     * The stage of the network interface definition allowing to specify private IP address within
     * a virtual network subnet.
     *
     * @param <T> the next stage after specifying the private IP configuration
     */
    interface DefinitionWithPrivateIp {
        /**
         * Enables dynamic private IP address allocation within the specified existing virtual network
         * subnet for the network interface's primary Ip configuration.
         *
         * @return the next stage of network interface definition
         */
        DefinitionWithPublicIpAddress withPrimaryPrivateIpAddressDynamic();

        /**
         * Assigns the specified static private IP address within the specified existing virtual network
         * subnet to the network interface's primary Ip configuration.
         *
         * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
         *                               the network interface
         * @return the next stage of network interface definition
         */
        DefinitionWithPublicIpAddress withPrimaryPrivateIpAddressStatic(String staticPrivateIpAddress);
    }

    /**
     * The stage of the network interface definition allowing to associate public IP address with it's primary
     * Ip configuration.
     */
    interface DefinitionWithPublicIpAddress extends
            DefinitionCreatable {
        /**
         * Create a new public IP address to associate with network interface's primary Ip configuration, based on
         * the provided definition.
         *
         * @param creatable a creatable definition for a new public IP
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withNewPrimaryPublicIpAddress(PublicIpAddress.DefinitionCreatable creatable);

        /**
         * Creates a new public IP address in the same region and group as the resource and associate it
         * with the network interface's primary Ip configuration.
         * <p>
         * the internal name and DNS label for the public IP address will be derived from the network interface name
         *
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withNewPrimaryPublicIpAddress();

        /**
         * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
         * and associate it with the network interface's primary Ip configuration.
         * <p>
         * the internal name for the public IP address will be derived from the DNS label
         *
         * @param leafDnsLabel the leaf domain label
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withNewPrimaryPublicIpAddress(String leafDnsLabel);

        /**
         * Associates an existing public IP address with the network interface's primary Ip configuration.
         *
         * @param publicIpAddress an existing public IP address
         * @return the next stage of the network interface definition
         */
        DefinitionCreatable withExistingPrimaryPublicIpAddress(PublicIpAddress publicIpAddress);
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
        DefinitionCreatable withIpForwarding();

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

        /**
         * Starts definition of a secondary Ip configuration.
         *
         * @return the first stage of a secondary Ip configuration definition
         */
        NicIpConfiguration.DefinitionBlank<DefinitionCreatable> defineSecondaryIpConfiguration(String name);
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
         * subnet for the network interface's primary Ip configuration.
         *
         * @return the next stage of network interface update
         */
        Update withPrimaryPrivateIpAddressDynamic();

        /**
         * Assigns the specified static private IP address within the specified existing virtual network
         * subnet to the network interface's primary Ip configuration.
         *
         * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
         *                               the primary Ip configuration
         * @return the next stage of network interface update
         */
        Update withPrimaryPrivateIpAddressStatic(String staticPrivateIpAddress);

        /**
         * Enable IP forwarding in the network interface.
         *
         * @return the next stage of the network interface update
         */
        Update withIpForwarding();

        /**
         * Disable IP forwarding in the network interface.
         *
         * @return the next stage of the network interface update
         */
        Update withoutIpForwarding();

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
         * Create a new public IP address to associate the network interface's primary Ip configuration,
         * based on the provided definition.
         * <p>
         * if there is public IP associated with the primary Ip configuration then that will be removed in
         * favour of this
         *
         * @param creatable a creatable definition for a new public IP
         * @return the next stage of the network interface update
         */
        Update withNewPrimaryPublicIpAddress(PublicIpAddress.DefinitionCreatable creatable);

        /**
         * Creates a new public IP address in the same region and group as the resource and associate it
         * with the network interface's primary Ip configuration.
         * <p>
         * the internal name and DNS label for the public IP address will be derived from the network interface name,
         * if there is an existing public IP association then that will be removed in favour of this
         *
         * @return the next stage of the network interface update
         */
        Update withNewPrimaryPublicIpAddress();

        /**
         * Creates a new public IP address in the same region and group as the resource, with the specified DNS label
         * and associate it with the network interface's primary Ip configuration.
         * <p>
         * the internal name for the public IP address will be derived from the DNS label, if there is an existing
         * public IP association then that will be removed in favour of this
         *
         * @param leafDnsLabel the leaf domain label
         * @return the next stage of the network interface update
         */
        Update withNewPrimaryPublicIpAddress(String leafDnsLabel);

        /**
         * Specifies that remove any public IP associated with the network interface's primary Ip configuration.
         *
         * @return the next stage of the network interface update
         */
        Update withoutPrimaryPublicIpAddress();

        /**
         * Associates an existing public IP address with the network interface's primary Ip configuration.
         * if there is an existing public IP association then that will be removed in favour of this
         *
         * @param publicIpAddress an existing public IP address
         * @return the next stage of the network interface update
         */
        Update withExistingPrimaryPublicIpAddress(PublicIpAddress publicIpAddress);

        /**
         * Starts definition of a secondary Ip configuration.
         *
         * @return the first stage of a secondary Ip configuration definition
         */
        NicIpConfiguration.DefinitionBlank<NetworkInterface.Update> defineSecondaryIpConfiguration(String name);

        /**
         * Starts update of an Ip configuration.
         *
         * @return the first stage of an Ip configuration update
         */
        NicIpConfiguration.Update<NetworkInterface.Update> updateIpConfiguration(String name);
    }
}