// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.inner.NetworkInterfaceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.Map;

/** Network interface. */
@Fluent
public interface NetworkInterface
    extends NetworkInterfaceBase,
        GroupableResource<NetworkManager, NetworkInterfaceInner>,
        Refreshable<NetworkInterface>,
        Updatable<NetworkInterface.Update>,
        UpdatableWithTags<NetworkInterface> {
    /** @return the IP configurations of this network interface, indexed by their names. */
    Map<String, NicIpConfiguration> ipConfigurations();

    /** @return the primary IP configuration of this network interface */
    NicIpConfiguration primaryIPConfiguration();

    /** The entirety of the network interface definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithPrimaryNetwork,
            DefinitionStages.WithPrimaryNetworkSubnet,
            DefinitionStages.WithPrimaryPrivateIP,
            DefinitionStages.WithCreate {
    }

    /** Grouping of network interface definition stages. */
    interface DefinitionStages {
        /** The stage of the network interface definition allowing to associate it with a load balancer. */
        interface WithLoadBalancer {
            /**
             * Associates the network interface's primary IP configuration with a backend of an existing load balancer.
             *
             * @param loadBalancer an existing load balancer
             * @param backendName the name of an existing backend on that load balancer
             * @return the next stage of the definition
             */
            WithCreate withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName);

            /**
             * Associates the network interface's primary IP configuration with an inbound NAT rule of an existing load
             * balancer.
             *
             * @param loadBalancer an existing load balancer
             * @param inboundNatRuleName the name of an existing inbound NAT rule on the selected load balancer
             * @return the next stage of the definition
             */
            WithCreate withExistingLoadBalancerInboundNatRule(LoadBalancer loadBalancer, String inboundNatRuleName);
        }

        /** The first stage of the network interface. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the network interface definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithPrimaryNetwork> {
        }

        /**
         * The stage of the network interface definition allowing to specify the virtual network for primary IP
         * configuration.
         */
        interface WithPrimaryNetwork {
            /**
             * Create a new virtual network to associate with the network interface's primary IP configuration, based on
             * the provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the definition
             */
            WithPrimaryPrivateIP withNewPrimaryNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the network interface's primary IP configuration.
             *
             * <p>The virtual network will be created in the same resource group and region as of network interface, it
             * will be created with the specified address space and a default subnet covering the entirety of the
             * network IP address space.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the definition
             */
            WithPrimaryPrivateIP withNewPrimaryNetwork(String name, String addressSpace);

            /**
             * Creates a new virtual network to associate with the network interface's primary IP configuration.
             *
             * <p>The virtual network will be created in the same resource group and region as of network interface, it
             * will be created with the specified address space and a default subnet covering the entirety of the
             * network IP address space.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the definition
             */
            WithPrimaryPrivateIP withNewPrimaryNetwork(String addressSpace);

            /**
             * Associate an existing virtual network with the network interface's primary IP configuration.
             *
             * @param network an existing virtual network
             * @return the next stage of the definition
             */
            WithPrimaryNetworkSubnet withExistingPrimaryNetwork(Network network);
        }

        /** The stage of the network interface definition allowing to specify subnet. */
        interface WithPrimaryNetworkSubnet {
            /**
             * Associate a subnet with the network interface's primary IP configuration.
             *
             * @param name the subnet name
             * @return the next stage of the definition
             */
            WithPrimaryPrivateIP withSubnet(String name);
        }

        /**
         * The stage of the network interface definition allowing to specify private IP address within a virtual network
         * subnet.
         */
        interface WithPrimaryPrivateIP {
            /**
             * Enables dynamic private IP address allocation within the specified existing virtual network subnet for
             * the network interface's primary IP configuration.
             *
             * @return the next stage of network interface definition
             */
            WithCreate withPrimaryPrivateIPAddressDynamic();

            /**
             * Assigns the specified static private IP address within the specified existing virtual network subnet to
             * the network interface's primary IP configuration.
             *
             * @param staticPrivateIPAddress the static IP address within the specified subnet to assign to the network
             *     interface
             * @return the next stage of network interface definition
             */
            WithCreate withPrimaryPrivateIPAddressStatic(String staticPrivateIPAddress);
        }

        /**
         * The stage of the network interface definition allowing to associate public IP address with it's primary IP
         * configuration.
         */
        interface WithPrimaryPublicIPAddress {
            /**
             * Create a new public IP address to associate with network interface's primary IP configuration, based on
             * the provided definition.
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the definition
             */
            WithCreate withNewPrimaryPublicIPAddress(Creatable<PublicIpAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associate it with the
             * network interface's primary IP configuration.
             *
             * <p>the internal name and DNS label for the public IP address will be derived from the network interface
             * name
             *
             * @return the next stage of the definition
             */
            WithCreate withNewPrimaryPublicIPAddress();

            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS
             * label and associate it with the network interface's primary IP configuration.
             *
             * <p>the internal name for the public IP address will be derived from the DNS label
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the definition
             */
            WithCreate withNewPrimaryPublicIPAddress(String leafDnsLabel);

            /**
             * Associates an existing public IP address with the network interface's primary IP configuration.
             *
             * @param publicIPAddress an existing public IP address
             * @return the next stage of the definition
             */
            WithCreate withExistingPrimaryPublicIPAddress(PublicIpAddress publicIPAddress);
        }

        /** The stage of the network interface definition allowing to associate a network security group. */
        interface WithNetworkSecurityGroup {
            /**
             * Create a new network security group to associate with network interface, based on the provided
             * definition.
             *
             * @param creatable a creatable definition for a new network security group
             * @return the next stage of the definition
             */
            WithCreate withNewNetworkSecurityGroup(Creatable<NetworkSecurityGroup> creatable);

            /**
             * Associates an existing network security group with the network interface.
             *
             * @param networkSecurityGroup an existing network security group
             * @return the next stage of the definition
             */
            WithCreate withExistingNetworkSecurityGroup(NetworkSecurityGroup networkSecurityGroup);
        }

        /** The stage of the network interface definition allowing to associate a secondary IP configurations. */
        interface WithSecondaryIPConfiguration {
            /**
             * Starts definition of a secondary IP configuration.
             *
             * @param name name for the IP configuration
             * @return the first stage of a secondary IP configuration definition
             */
            NicIpConfiguration.DefinitionStages.Blank<WithCreate> defineSecondaryIPConfiguration(String name);
        }

        /** The stage of the network interface definition allowing to enable accelerated networking. */
        interface WithAcceleratedNetworking {
            /**
             * Enables accelerated networking.
             *
             * <p>Note that additional steps need to be taken in the virtual machine itself for the virtual machine
             * associated with this network interface to be able to take advantage of accelerated networking. This
             * feature might not be available in some regions, virtual machine sizes, or operating system versions. It
             * can be enabled only during the creation of a network interface, not during an update.
             *
             * @return the next stage of the definition
             */
            WithCreate withAcceleratedNetworking();
        }

        /**
         * The stage of the network interface definition which contains all the minimum required inputs for the resource
         * to be created, but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<NetworkInterface>,
                Resource.DefinitionWithTags<WithCreate>,
                WithPrimaryPublicIPAddress,
                WithNetworkSecurityGroup,
                WithSecondaryIPConfiguration,
                WithAcceleratedNetworking,
                WithLoadBalancer {
            /**
             * Enables IP forwarding in the network interface.
             *
             * @return the next stage of the definition
             */
            WithCreate withIPForwarding();

            /**
             * Specifies the IP address of the custom DNS server to associate with the network interface.
             *
             * <p>Note this method's effect is additive, i.e. each time it is used, the new dns server is added to the
             * network interface.
             *
             * @param ipAddress the IP address of the DNS server
             * @return the next stage of the definition
             */
            WithCreate withDnsServer(String ipAddress);

            /**
             * Specifies the internal DNS name label for the network interface.
             *
             * @param dnsNameLabel the internal DNS name label
             * @return the next stage of the definition
             */
            WithCreate withInternalDnsNameLabel(String dnsNameLabel);

            /**
             * Begins creating the network interface resource.
             *
             * @return the accepted create operation
             */
            Accepted<NetworkInterface> beginCreate();
        }
    }

    /** Grouping of network interface update stages. */
    interface UpdateStages {
        /** The stage of the network interface update allowing to specify subnet. */
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
         * The stage of the network interface update allowing to specify private IP address within a virtual network
         * subnet.
         */
        interface WithPrimaryPrivateIP {
            /**
             * Enables dynamic private IP address allocation within the specified existing virtual network subnet for
             * the network interface's primary IP configuration.
             *
             * @return the next stage of network interface update
             */
            Update withPrimaryPrivateIPAddressDynamic();

            /**
             * Assigns the specified static private IP address within the specified existing virtual network subnet to
             * the network interface's primary IP configuration.
             *
             * @param staticPrivateIPAddress the static IP address within the specified subnet to assign to the primary
             *     IP configuration
             * @return the next stage of network interface update
             */
            Update withPrimaryPrivateIPAddressStatic(String staticPrivateIPAddress);
        }

        /**
         * The stage of the network interface update allowing to associate public IP address with it's primary IP
         * configuration.
         */
        interface WithPrimaryPublicIPAddress {
            /**
             * Create a new public IP address to associate the network interface's primary IP configuration, based on
             * the provided definition.
             *
             * <p>if there is public IP associated with the primary IP configuration then that will be removed in favour
             * of this
             *
             * @param creatable a creatable definition for a new public IP
             * @return the next stage of the network interface update
             */
            Update withNewPrimaryPublicIPAddress(Creatable<PublicIpAddress> creatable);

            /**
             * Creates a new public IP address in the same region and group as the resource and associate it with the
             * network interface's primary IP configuration.
             *
             * <p>the internal name and DNS label for the public IP address will be derived from the network interface
             * name, if there is an existing public IP association then that will be removed in favour of this
             *
             * @return the next stage of the network interface update
             */
            Update withNewPrimaryPublicIPAddress();

            /**
             * Creates a new public IP address in the same region and group as the resource, with the specified DNS
             * label and associate it with the network interface's primary IP configuration.
             *
             * <p>the internal name for the public IP address will be derived from the DNS label, if there is an
             * existing public IP association then that will be removed in favour of this
             *
             * @param leafDnsLabel the leaf domain label
             * @return the next stage of the network interface update
             */
            Update withNewPrimaryPublicIPAddress(String leafDnsLabel);

            /**
             * Specifies that remove any public IP associated with the network interface's primary IP configuration.
             *
             * @return the next stage of the network interface update
             */
            Update withoutPrimaryPublicIPAddress();

            /**
             * Associates an existing public IP address with the network interface's primary IP configuration. if there
             * is an existing public IP association then that will be removed in favour of this
             *
             * @param publicIPAddress an existing public IP address
             * @return the next stage of the network interface update
             */
            Update withExistingPrimaryPublicIPAddress(PublicIpAddress publicIPAddress);
        }

        /** The stage of the network interface update allowing to associate network security group. */
        interface WithNetworkSecurityGroup {
            /**
             * Create a new network security group to associate with network interface, based on the provided
             * definition.
             *
             * @param creatable a creatable definition for a new network security group
             * @return the next stage of the network interface update
             */
            Update withNewNetworkSecurityGroup(Creatable<NetworkSecurityGroup> creatable);

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

        /** The stage of the network interface update allowing to enable or disable IP forwarding. */
        interface WithIPForwarding {
            /**
             * Enable IP forwarding in the network interface.
             *
             * @return the next stage of the network interface update
             */
            Update withIPForwarding();

            /**
             * Disable IP forwarding in the network interface.
             *
             * @return the next stage of the network interface update
             */
            Update withoutIPForwarding();
        }

        /** The stage of the network interface update allowing to specify DNS servers. */
        interface WithDnsServer {
            /**
             * Specifies the IP address of the custom DNS server to associate with the network interface.
             *
             * <p>Note this method's effect is additive, i.e. each time it is used, the new dns server is added to the
             * network interface.
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
             *
             * <p>Using azure DNS server will remove any custom DNS server associated with this network interface.
             *
             * @return the next stage of the network interface update
             */
            Update withAzureDnsServer();
        }

        /** The stage of the network interface update allowing to configure IP configuration. */
        interface WithIPConfiguration {
            /**
             * Starts definition of a secondary IP configuration.
             *
             * @param name name for the IP configuration
             * @return the first stage of the update
             */
            NicIpConfiguration.UpdateDefinitionStages.Blank<NetworkInterface.Update> defineSecondaryIPConfiguration(
                String name);

            /**
             * Starts update of an IP configuration.
             *
             * @param name name of the IP configuration
             * @return the first stage of the update
             */
            NicIpConfiguration.Update updateIPConfiguration(String name);

            /**
             * Removes the specified IP configuration.
             *
             * @param name the name of an existing IP configuration
             * @return the next stage of the update
             */
            Update withoutIPConfiguration(String name);
        }

        /** The stage of the network interface definition allowing to disable accelerated networking. */
        interface WithAcceleratedNetworking {
            /**
             * Disables accelerated networking.
             *
             * @return the next stage of the update
             */
            Update withoutAcceleratedNetworking();
        }

        /** The stage of the network interface update allowing to associate it with a load balancer. */
        interface WithLoadBalancer {
            /**
             * Associates the network interface's primary IP configuration with a backend of an existing load balancer.
             *
             * @param loadBalancer an existing load balancer
             * @param backendName the name of an existing backend on that load balancer
             * @return the next stage of the update
             */
            Update withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName);

            /**
             * Associates the network interface's primary IP configuration with an inbound NAT rule of an existing load
             * balancer.
             *
             * @param loadBalancer an existing load balancer
             * @param inboundNatRuleName the name of an existing inbound NAT rule on the selected load balancer
             * @return the next stage of the update
             */
            Update withExistingLoadBalancerInboundNatRule(LoadBalancer loadBalancer, String inboundNatRuleName);

            /**
             * Removes all the existing associations with any load balancer backends.
             *
             * @return the next stage of the update
             */
            Update withoutLoadBalancerBackends();

            /**
             * Removes all the existing associations with any load balancer inbound NAT rules.
             *
             * @return the next stage of the update
             */
            Update withoutLoadBalancerInboundNatRules();
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<NetworkInterface>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithPrimaryNetworkSubnet,
            UpdateStages.WithPrimaryPrivateIP,
            UpdateStages.WithPrimaryPublicIPAddress,
            UpdateStages.WithNetworkSecurityGroup,
            UpdateStages.WithIPForwarding,
            UpdateStages.WithDnsServer,
            UpdateStages.WithIPConfiguration,
            UpdateStages.WithLoadBalancer,
            UpdateStages.WithAcceleratedNetworking {
    }
}
