/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkInterfaceIPConfigurationInner;
import com.microsoft.azure.management.network.model.HasPrivateIPAddress;
import com.microsoft.azure.management.network.model.HasPublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An IP configuration in a network interface.
 */
@Fluent()
public interface NicIPConfiguration extends
        NicIPConfigurationBase,
        HasInner<NetworkInterfaceIPConfigurationInner>,
        ChildResource<NetworkInterface>,
        HasPrivateIPAddress,
        HasPublicIPAddress,
        HasSubnet {
    /**
     * The entirety of the network interface IP configuration definition.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithNetwork<ParentT>,
        DefinitionStages.WithSubnet<ParentT>,
        DefinitionStages.WithPrivateIP<ParentT> {
    }

    /**
     * Grouping of network interface IP configuration definition stages applicable as part of a
     * network interface update.
     */
    interface DefinitionStages {
        /**
         * The first stage of network interface IP configuration definition.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithNetwork<ParentT> {
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the virtual network.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithNetwork<ParentT> {
            /**
             * Create a new virtual network to associate with the  network interface IP configuration,
             * based on the provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent
             * network interface, it will be created with the specified address space and a default subnet
             * covering the entirety of the network IP address space.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withNewNetwork(String name, String addressSpace);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent network interface,
             * it will be created with the specified address space and a default subnet covering the entirety of the
             * network IP address space.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withNewNetwork(String addressSpace);

            /**
             * Associate an existing virtual network with the network interface IP configuration.
             *
             * @param network an existing virtual network
             * @return the next stage of the definition
             */
            WithSubnet<ParentT> withExistingNetwork(Network network);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify private IP address
         * within a virtual network subnet.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPrivateIP<ParentT> extends HasPrivateIPAddress.DefinitionStages.WithPrivateIPAddress<WithAttach<ParentT>> {
            /**
             * Specifies the IP version for the private IP address.
             * @param ipVersion an IP version
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPrivateIPVersion(IPVersion ipVersion);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify subnet.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithSubnet<ParentT> {
            /**
             * Associate a subnet with the network interface IP configuration.
             *
             * @param name the subnet name
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withSubnet(String name);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to associate it with
         * a public IP address.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPublicIPAddress<ParentT> extends HasPublicIPAddress.DefinitionStages.WithPublicIPAddress<WithAttach<ParentT>> {
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the load balancer
         * to associate this IP configuration with.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithLoadBalancer<ParentT> {
            /**
             * Specifies the load balancer backend to associate this IP configuration with.
             * @param loadBalancer an existing load balancer
             * @param backendName the name of an existing backend on that load balancer
             * @return the next stage of the update
             */
            WithAttach<ParentT> withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName);

            /**
             * Specifies the load balancer inbound NAT rule to associate this IP configuration with.
             * @param loadBalancer an existing load balancer
             * @param inboundNatRuleName the name of an existing inbound NAT rule on the selected load balancer
             * @return the next stage of the update
             */
            WithAttach<ParentT> withExistingLoadBalancerInboundNatRule(LoadBalancer loadBalancer, String inboundNatRuleName);
        }

        /**
         * The final stage of network interface IP configuration.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the network interface IP configuration
         * definition can be attached to the parent network interface definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithAttach<ParentT>
                extends
                Attachable.InDefinition<ParentT>,
                WithPublicIPAddress<ParentT>,
                WithLoadBalancer<ParentT> {
        }
    }

    /** The entirety of a network interface IP configuration definition as part of a network interface update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithNetwork<ParentT>,
            UpdateDefinitionStages.WithPrivateIP<ParentT>,
            UpdateDefinitionStages.WithSubnet<ParentT>,
            UpdateDefinitionStages.WithPublicIPAddress<ParentT> {
    }

    /**
     * Grouping of network interface IP configuration definition stages.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of network interface IP configuration definition.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithNetwork<ParentT> {
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the virtual network.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithNetwork<ParentT> {
            /**
             * Create a new virtual network to associate with the  network interface IP configuration,
             * based on the provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent
             * network interface, it will be created with the specified address space and a default subnet
             * covering the entirety of the network IP address space.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withNewNetwork(String name, String addressSpace);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent network interface,
             * it will be created with the specified address space and a default subnet covering the entirety of the
             * network IP address space.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withNewNetwork(String addressSpace);

            /**
             * Associate an existing virtual network with the network interface IP configuration.
             *
             * @param network an existing virtual network
             * @return the next stage of the definition
             */
            WithSubnet<ParentT> withExistingNetwork(Network network);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify private IP address
         * within a virtual network subnet.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPrivateIP<ParentT> extends HasPrivateIPAddress.UpdateDefinitionStages.WithPrivateIPAddress<WithAttach<ParentT>> {
            /**
             * Specifies the IP version for the private IP address.
             * @param ipVersion an IP version
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPrivateIPVersion(IPVersion ipVersion);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify subnet.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithSubnet<ParentT> {
            /**
             * Associate a subnet with the network interface IP configuration.
             *
             * @param name the subnet name
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withSubnet(String name);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to associate it with
         * a public IP address.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPublicIPAddress<ParentT> extends HasPublicIPAddress.UpdateDefinitionStages.WithPublicIPAddress<WithAttach<ParentT>> {
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the load balancer
         * to associate this IP configuration with.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithLoadBalancer<ParentT> {
            /**
             * Specifies the load balancer to associate this IP configuration with.
             * @param loadBalancer an existing load balancer
             * @param backendName the name of an existing backend on that load balancer
             * @return the next stage of the update
             */
            WithAttach<ParentT> withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName);

            /**
             * Specifies the load balancer inbound NAT rule to associate this IP configuration with.
             * @param loadBalancer an existing load balancer
             * @param inboundNatRuleName the name of an existing inbound NAT rule on the selected load balancer
             * @return the next stage of the update
             */
            WithAttach<ParentT> withExistingLoadBalancerInboundNatRule(LoadBalancer loadBalancer, String inboundNatRuleName);
        }

        /**
         * The final stage of network interface IP configuration.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the network interface IP configuration
         * definition can be attached to the parent network interface definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithAttach<ParentT>
                extends
                Attachable.InUpdate<ParentT>,
                WithPublicIPAddress<ParentT> {
        }
    }

    /**
     * The entirety of a network interface IP configuration update as part of a network interface update.
     */
    interface Update extends
        Settable<NetworkInterface.Update>,
        UpdateStages.WithSubnet,
        UpdateStages.WithPrivateIP,
        UpdateStages.WithPublicIPAddress,
        UpdateStages.WithLoadBalancer {
    }

    /**
     * Grouping of network interface IP configuration update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the network interface IP configuration update allowing to specify subnet.
         */
        interface WithSubnet {
            /**
             * Associate a subnet with the network interface IP configuration.
             *
             * @param name the subnet name
             * @return the next stage of the network interface IP configuration update
             */
            Update withSubnet(String name);
        }

        /**
         * The stage of the network interface IP configuration update allowing to specify private IP.
         */
        interface WithPrivateIP extends HasPrivateIPAddress.UpdateStages.WithPrivateIPAddress<Update> {
            /**
             * Specifies the IP version for the private IP address.
             * @param ipVersion an IP version
             * @return the next stage of the update
             */
            Update withPrivateIPVersion(IPVersion ipVersion);
        }

        /**
         * The stage of the network interface IP configuration update allowing to specify public IP address.
         */
        interface WithPublicIPAddress extends HasPublicIPAddress.UpdateStages.WithPublicIPAddress<Update> {
        }

        /**
         * The stage of the network interface's IP configuration allowing to specify the load balancer
         * to associate this IP configuration with.
         */
        interface WithLoadBalancer {
            /**
             * Specifies the load balancer to associate this IP configuration with.
             * @param loadBalancer an existing load balancer
             * @param backendName the name of an existing backend on that load balancer
             * @return the next stage of the update
             */
            Update withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName);

            /**
             * Specifies the load balancer inbound NAT rule to associate this IP configuration with.
             * @param loadBalancer an existing load balancer
             * @param inboundNatRuleName the name of an existing inbound NAT rule on the selected load balancer
             * @return the next stage of the update
             */
            Update withExistingLoadBalancerInboundNatRule(LoadBalancer loadBalancer, String inboundNatRuleName);

            /**
             * Removes all the existing associations with load balancer backends.
             * @return the next stage of the update
             */
            Update withoutLoadBalancerBackends();

            /**
             * Removes all the existing associations with load balancer inbound NAT rules.
             * @return the next stage of the update
             */
            Update withoutLoadBalancerInboundNatRules();
        }
    }
}
