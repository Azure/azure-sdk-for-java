package com.microsoft.azure.management.network;

import java.util.List;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.NetworkInterfaceIPConfigurationInner;
import com.microsoft.azure.management.network.model.HasPrivateIpAddress;
import com.microsoft.azure.management.network.model.HasPublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An IP configuration in a network interface.
 */
@Fluent()
public interface NicIpConfiguration extends
        Wrapper<NetworkInterfaceIPConfigurationInner>,
        ChildResource<NetworkInterface>,
        HasPrivateIpAddress,
        HasPublicIpAddress,
        HasSubnet {
    // Getters

    /**
     * @return the virtual network associated with this IP configuration
     */
    Network getNetwork();

    /**
     * @return private IP address version
     */
    IPVersion privateIpAddressVersion();

    /**
     * @return the load balancer backends associated with this network interface IP configuration
     */
    List<Backend> listAssociatedLoadBalancerBackends();

    /**
     * @return the load balancer inbound NAT rules associated with this network interface IP configuration
     */
    List<InboundNatRule> listAssociatedLoadBalancerInboundNatRules();

    // Setters (fluent)

    /**
     * The entirety of the network interface IP configuration definition.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithNetwork<ParentT>,
        DefinitionStages.WithSubnet<ParentT>,
        DefinitionStages.WithPrivateIp<ParentT> {
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
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent
             * network interface, it will be created with the specified address space and a default subnet
             * covering the entirety of the network IP address space.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(String name, String addressSpace);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent network interface,
             * it will be created with the specified address space and a default subnet covering the entirety of the
             * network IP address space.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(String addressSpace);

            /**
             * Associate an existing virtual network with the network interface IP configuration.
             *
             * @param network an existing virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithSubnet<ParentT> withExistingNetwork(Network network);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify private IP address
         * within a virtual network subnet.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPrivateIp<ParentT> extends HasPrivateIpAddress.DefinitionStages.WithPrivateIpAddress<WithAttach<ParentT>> {
            /**
             * Specifies the IP version for the private IP address.
             * @param ipVersion an IP version
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPrivateIpVersion(IPVersion ipVersion);
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
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withSubnet(String name);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to associate it with
         * a public IP address.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPublicIpAddress<ParentT> extends HasPublicIpAddress.DefinitionStages.WithPublicIpAddress<WithAttach<ParentT>> {
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
                WithPublicIpAddress<ParentT>,
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
            UpdateDefinitionStages.WithPrivateIp<ParentT>,
            UpdateDefinitionStages.WithSubnet<ParentT>,
            UpdateDefinitionStages.WithPublicIpAddress<ParentT> {
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
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(Creatable<Network> creatable);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent
             * network interface, it will be created with the specified address space and a default subnet
             * covering the entirety of the network IP address space.
             *
             * @param name the name of the new virtual network
             * @param addressSpace the address space for rhe virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(String name, String addressSpace);

            /**
             * Creates a new virtual network to associate with the network interface IP configuration.
             * <p>
             * the virtual network will be created in the same resource group and region as of parent network interface,
             * it will be created with the specified address space and a default subnet covering the entirety of the
             * network IP address space.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withNewNetwork(String addressSpace);

            /**
             * Associate an existing virtual network with the network interface IP configuration.
             *
             * @param network an existing virtual network
             * @return the next stage of the network interface IP configuration definition
             */
            WithSubnet<ParentT> withExistingNetwork(Network network);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to specify private IP address
         * within a virtual network subnet.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPrivateIp<ParentT> extends HasPrivateIpAddress.UpdateDefinitionStages.WithPrivateIpAddress<WithAttach<ParentT>> {
            /**
             * Specifies the IP version for the private IP address.
             * @param ipVersion an IP version
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPrivateIpVersion(IPVersion ipVersion);
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
             * @return the next stage of the network interface IP configuration definition
             */
            WithPrivateIp<ParentT> withSubnet(String name);
        }

        /**
         * The stage of the network interface IP configuration definition allowing to associate it with
         * a public IP address.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithPublicIpAddress<ParentT> extends HasPublicIpAddress.UpdateDefinitionStages.WithPublicIpAddress<WithAttach<ParentT>> {
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
                WithPublicIpAddress<ParentT> {
        }
    }

    /**
     * The entirety of a network interface IP configuration update as part of a network interface update.
     */
    interface Update extends
        Settable<NetworkInterface.Update>,
        UpdateStages.WithSubnet,
        UpdateStages.WithPrivateIp,
        UpdateStages.WithPublicIpAddress,
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
        interface WithPrivateIp extends HasPrivateIpAddress.UpdateStages.WithPrivateIpAddress<Update> {
            /**
             * Specifies the IP version for the private IP address.
             * @param ipVersion an IP version
             * @return the next stage of the update
             */
            Update withPrivateIpVersion(IPVersion ipVersion);
        }

        /**
         * The stage of the network interface IP configuration update allowing to specify public IP address.
         */
        interface WithPublicIpAddress extends HasPublicIpAddress.UpdateStages.WithPublicIpAddress<Update> {
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
