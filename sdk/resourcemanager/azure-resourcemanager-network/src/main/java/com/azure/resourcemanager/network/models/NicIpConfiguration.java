// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.inner.NetworkInterfaceIpConfigurationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** An IP configuration in a network interface. */
@Fluent()
public interface NicIpConfiguration
    extends NicIpConfigurationBase,
        HasInner<NetworkInterfaceIpConfigurationInner>,
        ChildResource<NetworkInterface>,
    HasPublicIpAddress {

    /**
     * The entirety of the network interface IP configuration definition.
     *
     * @param <ParentT> the stage of the parent network interface definition to return to after attaching this
     *     definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithNetwork<ParentT>,
            DefinitionStages.WithSubnet<ParentT>,
            DefinitionStages.WithPrivateIP<ParentT> {
    }

    /**
     * Grouping of network interface IP configuration definition stages applicable as part of a network interface
     * update.
     */
    interface DefinitionStages {
        /**
         * The first stage of network interface IP configuration definition.
         *
         * @param <ParentT> the stage of the parent network interface definition to return to after attaching this
         *     definition
         */
        interface Blank<ParentT> extends WithNetwork<ParentT> {
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the virtual network.
         *
         * @param <ParentT> the stage of the parent network interface definition to return to after attaching this
         *     definition
         */
        interface WithNetwork<ParentT> {
            /**
             * Create a new virtual network to associate with the network interface IP configuration, based on the
             * provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             *
             * <p>the virtual network will be created in the same resource group and region as of parent network
             * interface, it will be created with the specified address space and a default subnet covering the entirety
             * of the network IP address space.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withNewNetwork(String name, String addressSpace);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             *
             * <p>the virtual network will be created in the same resource group and region as of parent network
             * interface, it will be created with the specified address space and a default subnet covering the entirety
             * of the network IP address space.
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
         * The stage of the network interface IP configuration definition allowing to specify private IP address within
         * a virtual network subnet.
         *
         * @param <ParentT> the stage of the parent network interface definition to return to after attaching this
         *     definition
         */
        interface WithPrivateIP<ParentT>
            extends HasPrivateIpAddress.DefinitionStages.WithPrivateIPAddress<WithAttach<ParentT>> {
            /**
             * Specifies the IP version for the private IP address.
             *
             * @param ipVersion an IP version
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPrivateIpVersion(IpVersion ipVersion);
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
         * The stage of the network interface IP configuration definition allowing to associate it with a public IP
         * address.
         *
         * @param <ParentT> the stage of the parent network interface definition to return to after attaching this
         *     definition
         */
        interface WithPublicIPAddress<ParentT>
            extends HasPublicIpAddress.DefinitionStages.WithPublicIPAddress<WithAttach<ParentT>> {
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the load balancer to
         * associate this IP configuration with.
         *
         * @param <ParentT> the stage of the parent network interface definition to return to after attaching this
         *     definition
         */
        interface WithLoadBalancer<ParentT> {
            /**
             * Specifies the load balancer backend to associate this IP configuration with.
             *
             * @param loadBalancer an existing load balancer
             * @param backendName the name of an existing backend on that load balancer
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName);

            /**
             * Specifies the load balancer inbound NAT rule to associate this IP configuration with.
             *
             * @param loadBalancer an existing load balancer
             * @param inboundNatRuleName the name of an existing inbound NAT rule on the selected load balancer
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingLoadBalancerInboundNatRule(
                LoadBalancer loadBalancer, String inboundNatRuleName);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the load balancer to
         * associate this IP configuration with.
         *
         * @param <ParentT> the stage of the parent network interface definition to return to after attaching this
         *     definition
         */
        interface WithApplicationGateway<ParentT> {
            /**
             * Specifies the application gateway backend to associate this IP configuration with.
             *
             * @param appGateway an existing application gateway
             * @param backendName the name of an existing backend on the application gateway
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingApplicationGatewayBackend(
                ApplicationGateway appGateway, String backendName);
        }

        /**
         * The final stage of network interface IP configuration.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the network interface IP configuration
         * definition can be attached to the parent network interface definition.
         *
         * @param <ParentT> the stage of the parent network interface definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
                WithPublicIPAddress<ParentT>,
                WithLoadBalancer<ParentT>,
                WithApplicationGateway<ParentT> {
        }
    }

    /**
     * The entirety of a network interface IP configuration definition as part of a network interface update.
     *
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithNetwork<ParentT>,
            UpdateDefinitionStages.WithPrivateIP<ParentT>,
            UpdateDefinitionStages.WithSubnet<ParentT>,
            UpdateDefinitionStages.WithPublicIPAddress<ParentT> {
    }

    /** Grouping of network interface IP configuration definition stages. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of network interface IP configuration definition.
         *
         * @param <ParentT> the stage of the parent network interface update to return to after attaching this
         *     definition
         */
        interface Blank<ParentT> extends WithNetwork<ParentT> {
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the virtual network.
         *
         * @param <ParentT> the stage of the parent network interface update to return to after attaching this
         *     definition
         */
        interface WithNetwork<ParentT> {
            /**
             * Create a new virtual network to associate with the network interface IP configuration, based on the
             * provided definition.
             *
             * @param creatable a creatable definition for a new virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             *
             * <p>the virtual network will be created in the same resource group and region as of parent network
             * interface, it will be created with the specified address space and a default subnet covering the entirety
             * of the network IP address space.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the definition
             */
            WithPrivateIP<ParentT> withNewNetwork(String name, String addressSpace);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             *
             * <p>the virtual network will be created in the same resource group and region as of parent network
             * interface, it will be created with the specified address space and a default subnet covering the entirety
             * of the network IP address space.
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
         * The stage of the network interface IP configuration definition allowing to specify private IP address within
         * a virtual network subnet.
         *
         * @param <ParentT> the stage of the parent network interface update to return to after attaching this
         *     definition
         */
        interface WithPrivateIP<ParentT>
            extends HasPrivateIpAddress.UpdateDefinitionStages.WithPrivateIPAddress<WithAttach<ParentT>> {
            /**
             * Specifies the IP version for the private IP address.
             *
             * @param ipVersion an IP version
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPrivateIpVersion(IpVersion ipVersion);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify subnet.
         *
         * @param <ParentT> the stage of the parent network interface update to return to after attaching this
         *     definition
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
         * The stage of the network interface IP configuration definition allowing to associate it with a public IP
         * address.
         *
         * @param <ParentT> the stage of the parent network interface update to return to after attaching this
         *     definition
         */
        interface WithPublicIPAddress<ParentT>
            extends HasPublicIpAddress.UpdateDefinitionStages.WithPublicIPAddress<WithAttach<ParentT>> {
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the load balancer to
         * associate this IP configuration with.
         *
         * @param <ParentT> the stage of the parent network interface update to return to after attaching this
         *     definition
         */
        interface WithLoadBalancer<ParentT> {
            /**
             * Specifies the load balancer to associate this IP configuration with.
             *
             * @param loadBalancer an existing load balancer
             * @param backendName the name of an existing backend on that load balancer
             * @return the next stage of the update
             */
            WithAttach<ParentT> withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName);

            /**
             * Specifies the load balancer inbound NAT rule to associate this IP configuration with.
             *
             * @param loadBalancer an existing load balancer
             * @param inboundNatRuleName the name of an existing inbound NAT rule on the selected load balancer
             * @return the next stage of the update
             */
            WithAttach<ParentT> withExistingLoadBalancerInboundNatRule(
                LoadBalancer loadBalancer, String inboundNatRuleName);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify the load balancer to
         * associate this IP configuration with.
         *
         * @param <ParentT> the stage of the parent network interface update to return to after attaching this
         *     definition
         */
        interface WithApplicationGateway<ParentT> {
            /**
             * Specifies the application gateway backend to associate this IP configuration with.
             *
             * @param appGateway an existing application gateway
             * @param backendName the name of an existing backend on the application gateway
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingApplicationGatewayBackend(
                ApplicationGateway appGateway, String backendName);
        }

        /**
         * The final stage of network interface IP configuration.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the network interface IP configuration
         * definition can be attached to the parent network interface definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the stage of the parent network interface update to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdate<ParentT>,
                WithPublicIPAddress<ParentT>,
                WithLoadBalancer<ParentT>,
                WithApplicationGateway<ParentT> {
        }
    }

    /** The entirety of a network interface IP configuration update as part of a network interface update. */
    interface Update
        extends Settable<NetworkInterface.Update>,
            UpdateStages.WithSubnet,
            UpdateStages.WithPrivateIP,
            UpdateStages.WithPublicIPAddress,
            UpdateStages.WithLoadBalancer,
            UpdateStages.WithApplicationGateway {
    }

    /** Grouping of network interface IP configuration update stages. */
    interface UpdateStages {
        /** The stage of the network interface IP configuration update allowing to specify subnet. */
        interface WithSubnet {
            /**
             * Associate a subnet with the network interface IP configuration.
             *
             * @param name the subnet name
             * @return the next stage of the network interface IP configuration update
             */
            Update withSubnet(String name);
        }

        /** The stage of the network interface IP configuration update allowing to specify private IP. */
        interface WithPrivateIP extends HasPrivateIpAddress.UpdateStages.WithPrivateIPAddress<Update> {
            /**
             * Specifies the IP version for the private IP address.
             *
             * @param ipVersion an IP version
             * @return the next stage of the update
             */
            Update withPrivateIpVersion(IpVersion ipVersion);
        }

        /** The stage of the network interface IP configuration update allowing to specify public IP address. */
        interface WithPublicIPAddress extends HasPublicIpAddress.UpdateStages.WithPublicIPAddress<Update> {
        }

        /**
         * The stage of the network interface's IP configuration allowing to specify the load balancer to associate this
         * IP configuration with.
         */
        interface WithLoadBalancer {
            /**
             * Specifies the load balancer to associate this IP configuration with.
             *
             * @param loadBalancer an existing load balancer
             * @param backendName the name of an existing backend on that load balancer
             * @return the next stage of the update
             */
            Update withExistingLoadBalancerBackend(LoadBalancer loadBalancer, String backendName);

            /**
             * Specifies the load balancer inbound NAT rule to associate this IP configuration with.
             *
             * @param loadBalancer an existing load balancer
             * @param inboundNatRuleName the name of an existing inbound NAT rule on the selected load balancer
             * @return the next stage of the update
             */
            Update withExistingLoadBalancerInboundNatRule(LoadBalancer loadBalancer, String inboundNatRuleName);

            /**
             * Removes all the existing associations with load balancer backends.
             *
             * @return the next stage of the update
             */
            Update withoutLoadBalancerBackends();

            /**
             * Removes all the existing associations with load balancer inbound NAT rules.
             *
             * @return the next stage of the update
             */
            Update withoutLoadBalancerInboundNatRules();
        }

        /**
         * The stage of the network interface IP configuration update allowing to specify the load balancer to associate
         * this IP configuration with.
         */
        interface WithApplicationGateway {
            /**
             * Specifies the application gateway backend to associate this IP configuration with.
             *
             * @param appGateway an existing application gateway
             * @param backendName the name of an existing backend on the application gateway
             * @return the next stage of the update
             */
            Update withExistingApplicationGatewayBackend(ApplicationGateway appGateway, String backendName);

            /**
             * Removes all existing associations with application gateway backends.
             *
             * @return the next stage of the update
             */
            Update withoutApplicationGatewayBackends();
        }
    }
}
