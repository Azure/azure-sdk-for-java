/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.LoadBalancerInner;
import com.microsoft.azure.management.network.model.HasLoadBalancingRules;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.network.model.HasPublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * Entry point for load balancer management API in Azure.
 */
@Fluent
public interface LoadBalancer extends
        GroupableResource,
        Refreshable<LoadBalancer>,
        Wrapper<LoadBalancerInner>,
        Updatable<LoadBalancer.Update>,
        HasLoadBalancingRules {

    // Getters
    /**
     * @return resource IDs of the public IP addresses assigned to the frontends of this load balancer
     */
    List<String> publicIpAddressIds();

    /**
     * @return TCP probes of this load balancer, indexed by the name
     */
    Map<String, LoadBalancerTcpProbe> tcpProbes();

    /**
     * @return HTTP probes of this load balancer, indexed by the name
     */
    Map<String, LoadBalancerHttpProbe> httpProbes();

    /**
     * @return backends for this load balancer to load balance the incoming traffic among, indexed by name
     */
    Map<String, LoadBalancerBackend> backends();

    /**
     * @return inbound NAT rules for this balancer
     */
    Map<String, LoadBalancerInboundNatRule> inboundNatRules();

    /**
     * @return frontends for this load balancer, for the incoming traffic to come from.
     */
    Map<String, LoadBalancerFrontend> frontends();

    /**
     * @return inbound NAT pools, indexed by name
     */
    Map<String, LoadBalancerInboundNatPool> inboundNatPools();

    /**
     * The entirety of the load balancer definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithFrontend,
        DefinitionStages.WithCreate,
        DefinitionStages.WithPublicFrontendOrBackend,
        DefinitionStages.WithPrivateFrontendOrBackend,
        DefinitionStages.WithNetworkSubnet,
        DefinitionStages.WithBackend,
        DefinitionStages.WithBackendOrProbe,
        DefinitionStages.WithProbe,
        DefinitionStages.WithProbeOrLoadBalancingRule,
        DefinitionStages.WithLoadBalancingRule,
        DefinitionStages.WithLoadBalancingRuleOrCreate,
        DefinitionStages.WithCreateAndInboundNatPool,
        DefinitionStages.WithCreateAndInboundNatRule,
        DefinitionStages.WithCreateAndNatChoice {
    }

    /**
     * Grouping of load balancer definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a load balancer definition.
         */
        interface Blank
            extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the load balancer definition allowing to specify the resource group.
         */
        interface WithGroup
            extends GroupableResource.DefinitionStages.WithGroup<WithFrontend> {
        }

        /**
         * The stage of a load balancer definition describing the nature of the frontend of the load balancer: internal or Internet-facing.
         */
        interface WithFrontend extends
            WithPublicIpAddress<WithPublicFrontendOrBackend>,
            WithPublicFrontend,
            WithPrivateFrontend {
        }

        /**
         * The stage of an internal load balancer definition allowing to define one or more private frontends.
         */
        interface WithPrivateFrontend extends WithNetworkSubnet {
            LoadBalancerPrivateFrontend.DefinitionStages.Blank<WithPrivateFrontendOrBackend> definePrivateFrontend(String name);
        }

        /**
         * The stage of an internal load balancer definition allowing to specify another private frontend or start specifying a backend.
         */
        interface WithPrivateFrontendOrBackend extends WithPrivateFrontend, WithBackend {
        }

        /**
         * The stage of an Internet-facing load balancer definition allowing to define one or more public frontends.
         */
        interface WithPublicFrontend {
            /**
             * Begins the definition of a new load public balancer frontend.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancerPublicFrontend.DefinitionStages.WithAttach#attach()}
             * @param name the name for the frontend
             * @return the first stage of the new frontend definition
             */
            LoadBalancerPublicFrontend.DefinitionStages.Blank<WithPublicFrontendOrBackend> definePublicFrontend(String name);
        }

        /**
         * The stage of an Internet-facing load balancer definition allowing to add additional public frontends
         * or add the first backend pool.
         */
        interface WithPublicFrontendOrBackend extends WithPublicFrontend, WithBackend {
        }

        /**
         * The stage of a load balancer definition allowing to add a backend.
         */
        interface WithBackend extends WithVirtualMachine<WithBackendOrProbe> {
            /**
             * Starts the definition of a backend.
             * @param name the name to assign to the backend
             * @return the next stage of the update
             */
            LoadBalancerBackend.DefinitionStages.Blank<WithBackendOrProbe> defineBackend(String name);
        }

        /**
         * The stage of a load balancer definition allowing to add a backend or start adding probes.
         */
        interface WithBackendOrProbe extends WithBackend, WithProbe {
        }

        /**
         * The stage of the load balancer definition allowing to add a load balancing probe.
         */
        interface WithProbe {
            /**
             * Adds a TCP probe checking the specified port.
             * <p>
             * The probe will be named using an automatically generated name.
             * @param port the port number for the probe to monitor
             * @return the next stage of the definition
             */
            WithProbeOrLoadBalancingRule withTcpProbe(int port);

            /**
             * Adds an HTTP probe checking for an HTTP 200 response from the specified path at regular intervals, using port 80.
             * <p>
             * An automatically generated name is assigned to the probe.
             * @param requestPath the path for the probe to invoke
             * @return the next stage of the definition
             */
            WithProbeOrLoadBalancingRule withHttpProbe(String requestPath);

            /**
             * Begins the definition of a new TCP probe to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancerTcpProbe.DefinitionStages.WithAttach#attach()}
             * @param name the name of the probe
             * @return the first stage of the new probe definition
             */
            LoadBalancerTcpProbe.DefinitionStages.Blank<WithProbeOrLoadBalancingRule> defineTcpProbe(String name);

            /**
             * Begins the definition of a new HTTP probe to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancerHttpProbe.DefinitionStages.WithAttach#attach()}
             * @param name the name of the probe
             * @return the first stage of the new probe definition
             */
            LoadBalancerHttpProbe.DefinitionStages.Blank<WithProbeOrLoadBalancingRule> defineHttpProbe(String name);
        }

        /**
         * The stage of a load balancer definition allowing to add another probe or start adding load balancing rules.
         */
        interface WithProbeOrLoadBalancingRule extends WithProbe, WithLoadBalancingRule {
        }

        /**
         * The stage of a load balancer definition allowing to add a virtual machine to
         * the load balancer's backend pool.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithVirtualMachine<ReturnT> {
            /**
             * Adds the specified set of virtual machines, assuming they are from the same
             * availability set, to this load balancer's back end address pool.
             * <p>
             * This will create a new backend address pool for this load balancer and add references to
             * the primary IP configurations of the primary network interfaces of each of the provided set of
             * virtual machines.
             * <p>
             * If the virtual machines are not in the same availability set, the load balancer will still
             * be created, but the virtual machines will not associated with its back end.
             * <p>
             * Only those virtual machines will be associated with the load balancer that already have an existing
             * network interface. Virtual machines without a network interface will be skipped.
             * @param vms existing virtual machines
             * @return the next stage of the update
             */
            ReturnT withExistingVirtualMachines(HasNetworkInterfaces...vms);
        }

        /**
         * The stage of a load balancer definition allowing to add a public IP address as the default public frontend.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIpAddress<ReturnT>
            extends HasPublicIpAddress.DefinitionStages.WithPublicIpAddress<ReturnT> {
        }

        /**
         * The stage of a load balancer definition allowing to specify an existing subnet as the private frontend.
         */
        interface WithNetworkSubnet {
            /**
             * Assigns the specified subnet from the selected network as teh default private frontend of this load balancer,
             * thereby making the load balancer internal.
             * <p>
             * Once the first private frontend is added, only private frontends can be added thereafter.
             * @param network an existing virtual network
             * @param subnetName the name of an existing subnet on the specified network
             * @return the next stage of the definition
             */
            WithPrivateFrontendOrBackend withFrontendSubnet(Network network, String subnetName);
        }

        /**
         * The stage of a load balancer definition allowing to create a load balancing rule.
         */
        interface WithLoadBalancingRule {
            /**
             * Creates a load balancing rule between the specified front end and back end ports and protocol.
             * <p>
             * The new rule will be assigned an automatically generated name.
             * @param frontendPort the port number on the front end to accept incoming traffic on
             * @param protocol the protocol to load balance
             * @param backendPort the port number on the back end to send load balanced traffic to
             * @return the next stage of the definition
             */
            WithLoadBalancingRuleOrCreate withLoadBalancingRule(int frontendPort, TransportProtocol protocol, int backendPort);

            /**
             * Creates a load balancing rule for the specified port and protocol and default frontend and backend associations.
             * <p>
             * The load balancing rule will created under the name "default". It will reference a backend, a frontend, and a load balancing probe all named "default".
             * @param port the port number on the front and back end for the network traffic to be load balanced on
             * @param protocol the protocol to load balance
             * @return the next stage of the definition
             */
            WithLoadBalancingRuleOrCreate withLoadBalancingRule(int port, TransportProtocol protocol);

            /**
             * Begins the definition of a new load balancing rule to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancingRule.DefinitionStages.WithAttach#attach()}
             * @param name the name of the load balancing rule
             * @return the first stage of the new load balancing rule definition
             */
            LoadBalancingRule.DefinitionStages.Blank<WithLoadBalancingRuleOrCreate> defineLoadBalancingRule(String name);
        }

        /**
         * The stage of a load balancer definition allowing to create a load balancing rule or create the load balancer.
         */
        interface WithLoadBalancingRuleOrCreate extends WithLoadBalancingRule, WithCreateAndNatChoice {
        }

        /**
         * The stage of a load balancer definition containing all the required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allowing
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<LoadBalancer>,
            Resource.DefinitionWithTags<WithCreate> {
        }

        /**
         * The stage of a load balancer definition allowing to create the load balancer or start configuring optional inbound NAT rules or pools.
         */
        interface WithCreateAndNatChoice extends
            WithCreate,
            WithInboundNatRule,
            WithInboundNatPool {
        }

        /**
         * The stage of a load balancer definition allowing to create the load balancer or add an inbound NAT pool.
         */
        interface WithCreateAndInboundNatPool extends
            WithCreate,
            WithInboundNatPool {
        }

        /**
         * The stage of a load balancer definition allowing to create the load balancer or add an inbound NAT rule.
         */
        interface WithCreateAndInboundNatRule extends
            WithCreate,
            WithInboundNatRule {
        }

        /**
         * The stage of a load balancer definition allowing to create a new inbound NAT rule.
         */
        interface WithInboundNatRule {
            /**
             * Begins the definition of a new inbound NAT rule to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancerInboundNatRule.DefinitionStages.WithAttach#attach()}
             * @param name the name of the inbound NAT rule
             * @return the first stage of the new inbound NAT rule definition
             */
            LoadBalancerInboundNatRule.DefinitionStages.Blank<WithCreateAndInboundNatRule> defineInboundNatRule(String name);
        }

        /**
         * The stage of a load balancer definition allowing to create a new inbound NAT pool for a virtual machine scale set.
         */
        interface WithInboundNatPool {
            /**
             * Begins the definition of a new inbount NAT pool to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancerInboundNatPool.DefinitionStages.WithAttach#attach()}
             * @param name the name of the inbound NAT pool
             * @return the first stage of the new inbound NAT pool definition
             */
            LoadBalancerInboundNatPool.DefinitionStages.Blank<WithCreateAndInboundNatPool> defineInboundNatPool(String name);
        }
    }

    /**
     * Grouping of load balancer update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the load balancer update allowing to add or remove backends.
         */
        interface WithBackend {
            /**
             * Removes the specified backend from the load balancer.
             * @param name the name of the backend to remove
             * @return the next stage of the update
             */
            Update withoutBackend(String name);

            /**
             * Begins the definition of a new backend as part of this load balancer update.
             * @param name the name for the new backend
             * @return the first stage of the backend definition
             */
            LoadBalancerBackend.UpdateDefinitionStages.Blank<Update> defineBackend(String name);

            /**
             * Begins the description of an update to an existing backend of this load balancer.
             * @param name the name of the backend to update
             * @return the first stage of the update
             */
            LoadBalancerBackend.Update updateBackend(String name);
        }

        /**
         * The stage of the load balancer update allowing to add, remove or modify probes.
         */
        interface WithProbe {
            /**
             * Adds a TCP probe checking the specified port.
             * <p>
             * The probe will be named using an automatically generated name.
             * @param port the port number for the probe to monitor
             * @return the next stage of the definition
             */
            Update withTcpProbe(int port);

            /**
             * Adds an HTTP probe checking for an HTTP 200 response from the specified path at regular intervals, using port 80.
             * <p>
             * An automatically generated name is assigned to the probe.
             * @param requestPath the path for the probe to invoke
             * @return the next stage of the definition
             */
            Update withHttpProbe(String requestPath);

            /**
             * Begins the definition of a new HTTP probe to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancerHttpProbe.DefinitionStages.WithAttach#attach()}
             * @param name the name of the new probe
             * @return the next stage of the definition
             */
            LoadBalancerHttpProbe.UpdateDefinitionStages.Blank<Update> defineHttpProbe(String name);

            /**
             * Begins the definition of a new TCP probe to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancerHttpProbe.DefinitionStages.WithAttach#attach()}
             * @param name the name of the new probe
             * @return the next stage of the definition
             */
            LoadBalancerTcpProbe.UpdateDefinitionStages.Blank<Update> defineTcpProbe(String name);

            /**
             * Removes the specified probe from the load balancer, if present.
             * @param name the name of the probe to remove
             * @return the next stage of the update
             */
            Update withoutProbe(String name);

            /**
             * Begins the description of an update to an existing TCP probe on this load balancer.
             * @param name the name of the probe to update
             * @return the first stage of the probe update
             */
            LoadBalancerTcpProbe.Update updateTcpProbe(String name);

            /**
             * Begins the description of an update to an existing HTTP probe on this load balancer.
             * @param name the name of the probe to update
             * @return the first stage of the probe update
             */
            LoadBalancerHttpProbe.Update updateHttpProbe(String name);
        }

        /**
         * The stage of the load balancer update allowing to add, remove or modify load balancing rules.
         */
        interface WithLoadBalancingRule {
            /**
             * Adds a load balancing rule between the specified front end and back end ports and protocol.
             * <p>
             * The new rule will be created under the name "default".
             * @param frontendPort the port number on the front end to accept incoming traffic on
             * @param protocol the protocol to load balance
             * @param backendPort the port number on the back end to send load balanced traffic to
             * @return the next stage of the definition
             */
            Update withLoadBalancingRule(int frontendPort, TransportProtocol protocol, int backendPort);

            /**
             * Adds a load balancing rule for the specified port and protocol.
             * <p>
             * The new rule will be created under the name "default".
             * @param port the port number on the front and back end for the network traffic to be load balanced on
             * @param protocol the protocol to load balance
             * @return the next stage of the definition
             */
            Update withLoadBalancingRule(int port, TransportProtocol protocol);

            /**
             * Begins the definition of a new load balancing rule to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancerTcpProbe.DefinitionStages.WithAttach#attach()}
             * @param name the name of the load balancing rule
             * @return the first stage of the new load balancing rule definition
             */
            LoadBalancingRule.UpdateDefinitionStages.Blank<Update> defineLoadBalancingRule(String name);

            /**
             * Removes the specified load balancing rule from the load balancer, if present.
             * @param name the name of the load balancing rule to remove
             * @return the next stage of the update
             */
            Update withoutLoadBalancingRule(String name);

            /**
             * Begins the description of an update to an existing load balancing rule on this load balancer.
             * @param name the name of the load balancing rule to update
             * @return the first stage of the load balancing rule update
             */
            LoadBalancingRule.Update updateLoadBalancingRule(String name);
        }

        /**
         * The stage of a load balancer update allowing to define, remove or edit Internet-facing frontends.
         */
        interface WithInternetFrontend extends WithPublicIpAddress {
            /**
             * Begins the update of a load balancer frontend.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancerPublicFrontend.UpdateDefinitionStages.WithAttach#attach()}
             * @param name the name for the frontend
             * @return the first stage of the new frontend definition
             */
            LoadBalancerPublicFrontend.UpdateDefinitionStages.Blank<Update> definePublicFrontend(String name);

            /**
             * Removes the specified frontend from the load balancer.
             * @param name the name of an existing front end on this load balancer
             * @return the next stage of the update
             */
            Update withoutFrontend(String name);

            /**
             * Begins the description of an update to an existing Internet-facing frontend.
             * @param name the name of the frontend to update
             * @return the first stage of the frontend update
             */
            LoadBalancerPublicFrontend.Update updateInternetFrontend(String name);
        }

        /**
         * The stage of a load balancer update allowing to add a public IP address as the default public frontend.
         */
        interface WithPublicIpAddress extends HasPublicIpAddress.UpdateDefinitionStages.WithPublicIpAddress<Update> {
        }

        /**
         * The stage of a load balancer update allowing to define one or more private frontends.
         */
        interface WithInternalFrontend extends WithNetworkSubnet {
            /**
             * Begins the update of an internal load balancer frontend.
             * @param name the name for the frontend
             * @return the first stage of the new frontend definition
             */
            LoadBalancerPrivateFrontend.UpdateDefinitionStages.Blank<Update> definePrivateFrontend(String name);

            /**
             * Begins the description of an update to an existing internal frontend.
             * @param name the name of an existing frontend from this load balancer
             * @return the first stage of the frontend update
             */
            LoadBalancerPrivateFrontend.Update updateInternalFrontend(String name);
        }

        /**
         * The stage of a load balancer update allowing to specify a subnet to assign to the load balancer's frontend.
         */
        interface WithNetworkSubnet {
            /**
             * Assigns the specified subnet from the specified network to the default frontend of this load balancer.
             * @param network an existing virtual network
             * @param subnetName the name of an existing subnet on the specified network
             * @return the next stage of the update
             */
            Update withFrontendSubnet(Network network, String subnetName);
        }

        /**
         * The stage of a load balancer update allowing to define, remove or edit inbound NAT rules.
         */
        interface WithInboundNatRule {
            /**
             * Removes the specified inbound NAT rule from the load balancer.
             * @param name the name of an existing inbound NAT rule on this load balancer
             * @return the next stage of the update
             */
            Update withoutInboundNatRule(String name);

            /**
             * Begins the definition of a new inbound NAT rule.
             * <p>
             * The definition must be completed with a call to {@link LoadBalancerInboundNatRule.UpdateDefinitionStages.WithAttach#attach()}
             * @param name the name for the inbound NAT rule
             * @return the first stage of the new inbound NAT rule definition
             */
            LoadBalancerInboundNatRule.UpdateDefinitionStages.Blank<Update> defineInboundNatRule(String name);

            /**
             * Begins the description of an update to an existing inbound NAT rule.
             * @param name the name of the inbound NAT rule to update
             * @return the first stage of the inbound NAT rule update
             */
            LoadBalancerInboundNatRule.Update updateInboundNatRule(String name);
        }

        /**
         * The stage of a load balancer update allowing to create a new inbound NAT pool for a virtual machine scale set.
         */
        interface WithInboundNatPool {
            /**
             * Removes the specified inbound NAT pool from the load balancer.
             * @param name the name of an existing inbound NAT pool on this load balancer
             * @return the next stage of the update
             */
            Update withoutInboundNatPool(String name);

            /**
             * Begins the definition of a new inbound NAT pool.
             * @param name the name of the inbound NAT pool
             * @return the first stage of the new inbound NAT pool definition
             */
            LoadBalancerInboundNatPool.UpdateDefinitionStages.Blank<Update> defineInboundNatPool(String name);

            /**
             * Begins the description of an update to an existing inbound NAT pool.
             * @param name the name of the inbound NAT pool to update
             * @return the first stage of the inbound NAT pool update
             */
            LoadBalancerInboundNatPool.Update updateInboundNatPool(String name);
        }
    }

    /**
     * The template for a load balancer update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<LoadBalancer>,
        Resource.UpdateWithTags<Update>,
        UpdateStages.WithProbe,
        UpdateStages.WithBackend,
        UpdateStages.WithLoadBalancingRule,
        UpdateStages.WithInternetFrontend,
        UpdateStages.WithInternalFrontend,
        UpdateStages.WithNetworkSubnet,
        UpdateStages.WithInboundNatRule,
        UpdateStages.WithInboundNatPool {
    }
}
