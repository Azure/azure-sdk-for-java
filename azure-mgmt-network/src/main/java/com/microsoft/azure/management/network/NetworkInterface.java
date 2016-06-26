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
    // Getters
    /**
     * @return <tt>true</tt> if Ip forwarding is enabled in this network interface
     */
    boolean isIpForwardingEnabled();

    /**
     * @return <tt>true</tt> if this is primary network interface in a virtual machine
     */
    boolean isPrimary();

    /**
     * @return the Mac Address of the network interface
     */
    String macAddress();

    /**
     *
     * @return the Internal Dns name assigned to this network interface
     */
    String internalDnsNameLabel();

    /**
     * Gets the fully qualified domain name of this network interface.
     * <p>
     * A network interface receives Fqdn as a part of assigning it to a virtual machine.
     *
     * @return the qualified domain name
     */
    String internalFqdn();

    /**
     * @return Ip addresses of this network interface's DNS servers
     */
    List<String> dnsServers();

    /**
     * Gets the public Ip address associated with this network interface.
     * <p>
     * This method makes a rest API call to fetch the public Ip.
     *
     * @return the public Ip associated with this network interface
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    PublicIpAddress primaryPublicIpAddress() throws CloudException, IOException;

    /**
     * @return the resource id of the virtual network subnet associated with this network interface.
     */
    String primarySubnetId();

    /**
     * Gets the virtual network associated this network interface's primary Ip configuration.
     * <p>
     * This method makes a rest API call to fetch the virtual network.
     *
     * @return the virtual network associated with this network interface.
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    Network primaryNetwork() throws CloudException, IOException;

    /**
     * Gets the private Ip address allocated to this network interface's primary Ip configuration.
     * <p>
     * The private Ip will be within the virtual network subnet of this network interface.
     *
     * @return the private Ip addresses
     */
    String primaryPrivateIp();

    /**
     * @return the private Ip allocation method (Dynamic, Static) of this network interface's
     * primary Ip configuration.
     */
    String primaryPrivateIpAllocationMethod();

    /**
     * @return the Ip configurations of this network interface
     */
    List<NicIpConfiguration> ipConfigurations();

    /**
     * @return the network security group resource id or null if there is no network security group
     * associated with this network interface.
     */
    String networkSecurityGroupId();

    /**
     * Gets the network security group associated this network interface.
     * <p>
     * This method makes a rest API call to fetch the Network Security Group resource.
     *
     * @return the network security group associated with this network interface.
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    NetworkSecurityGroup networkSecurityGroup() throws CloudException, IOException;

    // Setters (fluent)

    /**
     * The entirety of the network interface definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithPrimaryNetwork,
            DefinitionStages.WithPrimaryNetworkSubnet,
            DefinitionStages.WithPrimaryPrivateIp,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of network interface definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the network interface.
         */
        interface Blank
                extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the network interface definition allowing to specify the resource group.
         */
        interface WithGroup
                extends GroupableResource.DefinitionWithGroup<WithPrimaryNetwork> {
        }

        /**
         * The stage of the network interface definition allowing to specify the virtual network for
         * primary Ip configuration.
         */
        interface WithPrimaryNetwork {
            /**
             * Create a new virtual network to associate with the network interface's primary Ip configuration,
             * based on the provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the network interface definition
             */
            WithPrimaryPrivateIp withNewPrimaryNetwork(Network.DefinitionStages.WithCreate creatable);

            /**
             * Creates a new virtual network to associate with the network interface's primary Ip configuration.
             * <p>
             * The virtual network will be created in the same resource group and region as of network interface,
             * it will be created with the specified address space and a default subnet covering the entirety of
             * the network IP address space.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the network interface definition
             */
            WithPrimaryPrivateIp withNewPrimaryNetwork(String name, String addressSpace);

            /**
             * Creates a new virtual network to associate with the network interface's primary Ip configuration.
             * <p>
             * The virtual network will be created in the same resource group and region as of network interface,
             * it will be created with the specified address space and a default subnet covering the entirety of
             * the network IP address space.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the network interface definition
             */
            WithPrimaryPrivateIp withNewPrimaryNetwork(String addressSpace);

            /**
             * Associate an existing virtual network with the network interface's primary Ip configuration.
             *
             * @param network an existing virtual network
             * @return the next stage of the network interface definition
             */
            WithPrimaryNetworkSubnet withExistingPrimaryNetwork(Network network);
        }

        /**
         * The stage of the network interface definition allowing to specify subnet.
         */
        interface WithPrimaryNetworkSubnet {
            /**
             * Associate a subnet with the network interface's primary Ip configuration.
             *
             * @param name the subnet name
             * @return the next stage of the network interface definition
             */
            WithPrimaryPrivateIp withSubnet(String name);
        }

        /**
         * The stage of the network interface definition allowing to specify private Ip address within
         * a virtual network subnet.
         */
        interface WithPrimaryPrivateIp {
            /**
             * Enables dynamic private IP address allocation within the specified existing virtual network
             * subnet for the network interface's primary Ip configuration.
             *
             * @return the next stage of network interface definition
             */
            WithCreate withPrimaryPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the specified existing virtual network
             * subnet to the network interface's primary Ip configuration.
             *
             * @param staticPrivateIpAddress the static IP address within the specified subnet to assign to
             *                               the network interface
             * @return the next stage of network interface definition
             */
            WithCreate withPrimaryPrivateIpAddressStatic(String staticPrivateIpAddress);
        }

        /**
         * The stage of the network interface definition allowing to associate public Ip address with it's primary
         * Ip configuration.
         */
        interface WithPrimaryPublicIpAddress {
            /**
             * Create a new public Ip address to associate with network interface's primary Ip configuration, based on
             * the provided definition.
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the network interface definition
             */
            WithCreate withNewPrimaryPublicIpAddress(PublicIpAddress.DefinitionStages.WithCreate creatable);

            /**
             * Creates a new public Ip address in the same region and group as the resource and associate it
             * with the network interface's primary Ip configuration.
             * <p>
             * the internal name and DNS label for the public IP address will be derived from the network interface name
             *
             * @return the next stage of the network interface definition
             */
            WithCreate withNewPrimaryPublicIpAddress();

            /**
             * Creates a new public Ip address in the same region and group as the resource, with the specified DNS label
             * and associate it with the network interface's primary Ip configuration.
             * <p>
             * the internal name for the public IP address will be derived from the DNS label
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the network interface definition
             */
            WithCreate withNewPrimaryPublicIpAddress(String leafDnsLabel);

            /**
             * Associates an existing public Ip address with the network interface's primary Ip configuration.
             *
             * @param publicIpAddress an existing public IP address
             * @return the next stage of the network interface definition
             */
            WithCreate withExistingPrimaryPublicIpAddress(PublicIpAddress publicIpAddress);
        }

        /**
         * The stage of the network interface definition allowing to associate a network security group.
         */
        interface WithNetworkSecurityGroup {
            /**
             * Create a new network security group to associate with network interface, based on the provided definition.
             *
             * @param creatable a creatable definition for a new network security group
             * @return the next stage of the network interface definition
             */
            WithCreate withNewNetworkSecurityGroup(NetworkSecurityGroup.DefinitionStages.WithCreate creatable);

            /**
             * Associates an existing network security group with the network interface.
             *
             * @param networkSecurityGroup an existing network security group
             * @return the next stage of the network interface definition
             */
            WithCreate withExistingNetworkSecurityGroup(NetworkSecurityGroup networkSecurityGroup);
        }

        /**
         * The stage of the network interface definition allowing to associate a secondary Ip configurations.
         */
        interface WithSecondaryIpConfiguration {
            /**
             * Starts definition of a secondary Ip configuration.
             *
             * @param name name for the Ip configuration
             * @return the first stage of a secondary Ip configuration definition
             */
            NicIpConfiguration.DefinitionStages.Blank<WithCreate> defineSecondaryIpConfiguration(String name);
        }

        /**
         * The stage of the network interface definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<NetworkInterface>,
                Resource.DefinitionWithTags<WithCreate>,
                WithPrimaryPublicIpAddress,
                WithNetworkSecurityGroup,
                WithSecondaryIpConfiguration {
            /**
             * Enable Ip forwarding in the network interface.
             *
             * @return the next stage of the network interface definition
             */
            WithCreate withIpForwarding();

            /**
             * Specifies the Ip address of the custom DNS server to associate with the network interface.
             * <p>
             * Note this method's effect is additive, i.e. each time it is used, the new dns server is
             * added to the network interface.
             *
             * @param ipAddress the IP address of the DNS server
             * @return the next stage of the network interface definition
             */
            WithCreate withDnsServer(String ipAddress);

            /**
             * Specifies the internal Dns name label for the network interface.
             *
             * @param dnsNameLabel the internal Dns name label
             * @return the next stage of the network interface definition
             */
            WithCreate withInternalDnsNameLabel(String dnsNameLabel);
        }
    }

    /**
     * Grouping of network interface update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the network interface update allowing to specify subnet.
         */
        interface WithPrimaryNetworkSubnet {
            /**
             * Associate a subnet with the network interface.
             *
             * @param name the subnet name
             * @return the next stage of the network interface update
             */
            Update withSubnet(String name);
        }

        /**
         * The stage of the network interface update allowing to specify private Ip address within
         * a virtual network subnet.
         */
        interface WithPrimaryPrivateIp {
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
        }

        /**
         * The stage of the network interface update allowing to associate public Ip address with it's primary
         * Ip configuration.
         */
        interface WithPrimaryPublicIpAddress {
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
            Update withNewPrimaryPublicIpAddress(PublicIpAddress.DefinitionStages.WithCreate creatable);

            /**
             * Creates a new public Ip address in the same region and group as the resource and associate it
             * with the network interface's primary Ip configuration.
             * <p>
             * the internal name and DNS label for the public IP address will be derived from the network interface name,
             * if there is an existing public IP association then that will be removed in favour of this
             *
             * @return the next stage of the network interface update
             */
            Update withNewPrimaryPublicIpAddress();

            /**
             * Creates a new public Ip address in the same region and group as the resource, with the specified DNS label
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
             * Specifies that remove any public Ip associated with the network interface's primary Ip configuration.
             *
             * @return the next stage of the network interface update
             */
            Update withoutPrimaryPublicIpAddress();

            /**
             * Associates an existing public Ip address with the network interface's primary Ip configuration.
             * if there is an existing public IP association then that will be removed in favour of this
             *
             * @param publicIpAddress an existing public IP address
             * @return the next stage of the network interface update
             */
            Update withExistingPrimaryPublicIpAddress(PublicIpAddress publicIpAddress);
        }

        /**
         * The stage of the network interface update allowing to associate network security group.
         */
        interface WithNetworkSecurityGroup {
            /**
             * Create a new network security group to associate with network interface, based on the provided definition.
             *
             * @param creatable a creatable definition for a new network security group
             * @return the next stage of the network interface update
             */
            Update withNewNetworkSecurityGroup(NetworkSecurityGroup.DefinitionStages.WithCreate creatable);

            /**
             * Associates an existing network security group with the network interface.
             *
             * @param networkSecurityGroup an existing network security group
             * @return the next stage of the network interface update
             */
            Update withExistingNetworkSecurityGroup(NetworkSecurityGroup networkSecurityGroup);

            /**
             * Specifies that remove any network security group associated with the network interface.
             *
             * @return the next stage of the network interface update
             */
            Update withoutNetworkSecurityGroup();
        }

        /**
         * The stage of the network interface update allowing to enable or disable Ip forwarding.
         */
        interface WithIpForwarding {
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
        }

        /**
         * The stage of the network interface update allowing to specify Dns servers.
         */
        interface WithDnsServer {
            /**
             * Specifies the IP address of the custom Dns server to associate with the network interface.
             * <p>
             * Note this method's effect is additive, i.e. each time it is used, the new dns server is
             * added to the network interface.
             *
             * @param ipAddress the Ip address of the Dns server
             * @return the next stage of the network interface update
             */
            Update withDnsServer(String ipAddress);

            /**
             * Removes a Dns server associated with the network interface.
             *
             * @param ipAddress the Ip address of the Dns server
             * @return the next stage of the network interface update
             */
            Update withoutDnsServer(String ipAddress);

            /**
             * Specifies to use the default Azure Dns server for the network interface.
             * <p>
             * Using azure Dns server will remove any custom Dns server associated with this network interface.
             *
             * @return the next stage of the network interface update
             */
            Update withAzureDnsServer();
        }

        /**
         * The stage of the network interface update allowing to configure Ip configuration.
         */
        interface WithIpConfiguration {
            /**
             * Starts definition of a secondary Ip configuration.
             *
             * @param name name for the Ip configuration
             * @return the first stage of a secondary Ip configuration definition
             */
            NicIpConfiguration.UpdateDefinitionStages.Blank<NetworkInterface.Update> defineSecondaryIpConfiguration(String name);

            /**
             * Starts update of an Ip configuration.
             *
             * @param name name of the Ip configuration
             * @return the first stage of an Ip configuration update
             */
            NicIpConfiguration.Update updateIpConfiguration(String name);
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
            Appliable<NetworkInterface>,
           Resource.UpdateWithTags<Update>,
            UpdateStages.WithPrimaryNetworkSubnet,
            UpdateStages.WithPrimaryPrivateIp,
            UpdateStages.WithPrimaryPublicIpAddress,
            UpdateStages.WithNetworkSecurityGroup,
            UpdateStages.WithIpForwarding,
            UpdateStages.WithDnsServer,
            UpdateStages.WithIpConfiguration {
    }
}