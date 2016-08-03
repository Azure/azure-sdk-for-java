/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.network.implementation.LoadBalancerInner;
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
public interface LoadBalancer extends
        GroupableResource,
        Refreshable<LoadBalancer>,
        Wrapper<LoadBalancerInner>,
        Updatable<LoadBalancer.Update> {

    // Getters
    /**
     * @return resource IDs of the public IP addresses assigned to the front end of this load balancer
     */
    List<String> publicIpAddressIds();

    /**
     * @return TCP probes of this load balancer, indexed by the name
     */
    Map<String, TcpProbe> tcpProbes();

    /**
     * @return HTTP probes of this load balancer, indexed by the name
     */
    Map<String, HttpProbe> httpProbes();

    /**
     * @return backends for this load balancer to load balance the incoming traffic among, indexed by name
     */
    Map<String, Backend> backends();

    /**
     * @return frontends for this load balancer, for the incoming traffic to come from.
     */
    Map<String, InternetFrontend> frontends();

    /**
     * @return load balancing rules, indexed by name
     */
    Map<String, LoadBalancingRule> loadBalancingRules();

    /**
     * The entirety of the load balancer definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithVirtualMachine,
        DefinitionStages.WithCreate,
        DefinitionStages.WithCreateAndRule,
        DefinitionStages.WithFrontends,
        DefinitionStages.WithInternetFrontendOrBackend,
        DefinitionStages.WithBackend,
        DefinitionStages.WithBackendOrCreate {
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
         * The stage of the definition describing the nature of the frontend of the load balancer: internal or Internet-facing.
         */
        interface WithFrontends extends
            WithPublicIpAddresses<WithInternetFrontendOrBackend>,
            WithInternetFrontends,
            WithInternalFrontends {
        }

        /**
         * The stage of the definition allowing to define one or more internal frontends.
         */
        interface WithInternalFrontends {
            // TODO
        }

        /**
         * The stage of the definition allowing to define one or more Internet-facing frontends.
         */
        interface WithInternetFrontends extends WithPublicIpAddresses<WithInternetFrontendOrBackend> {
            /**
             * Begins the definition of a new load balancer frontend.
             * <p>
             * The definition must be completed with a call to {@link InternetFrontend.DefinitionStages.WithAttach#attach()}
             * @param name the name for the frontend
             * @return the first stage of the new frontend definition
             */
            InternetFrontend.DefinitionStages.Blank<WithInternetFrontendOrBackend> defineInternetFrontend(String name);
        }

        /**
         * The stage of the definition allowing to add additional Internet-facing frontends or add the first back end pool.
         */
        interface WithInternetFrontendOrBackend extends WithInternetFrontends, WithBackend {
        }

        /**
         * The stage of the definition allowing to add a backend.
         */
        interface WithBackend extends WithVirtualMachine {
            /**
             * Adds a new empty backend to the load balancer.
             * @param name the name to assign to the backend
             * @return the next stage of the update
             */
            WithBackendOrCreate withBackend(String name);
        }

        /**
         * The stage of the definition allowing to add another backend pool or the first load balancing rule.
         */
        interface WithBackendOrCreate extends WithBackend, WithCreate {
        }

        /**
         * The stage of the load balancer definition allowing to specify the resource group.
         */
        interface WithGroup
            extends GroupableResource.DefinitionStages.WithGroup<WithFrontends> {
        }

        /**
         * The stage of the load balancer definition allowing to add a virtual machine to
         * the load balancer's backend pool.
         */
        interface WithVirtualMachine {
            /**
             * Adds the specified set of virtual machines, assuming they are from the same
             * availability set, to this load balancer's back end address pool.
             * <p>
             * This will create a new back end address pool for this load balancer and add references to
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
            WithBackendOrCreate withExistingVirtualMachines(SupportsNetworkInterfaces...vms);

            /**
             * Adds the specified set of virtual machines, assuming they are from the same
             * availability set, to the specified backend of this load balancer.
             * <p>
             * If an existing backend with the provided name does not exist on this load balancer, it will be created.
             * If the name is null, a new name for the backend will be generated automatically.
             * <p>
             * Reference to the primary IP configurations of the primary network interfaces of each of the provided set of
             * virtual machines will be added to the backend.
             * <p>
             * If the virtual machines are not in the same availability set, the load balancer will still
             * be created, but the virtual machines will not be associated it.
             * <p>
             * Only those virtual machines will be associated with the load balancer that already have an existing
             * network interface. Virtual machines without a network interface will be skipped.
             * @param backendName the name of the backend to associate the virtual machines with
             * @param vms existing virtual machines
             * @return the next stage of the update
             */
            WithBackendOrCreate withExistingVirtualMachines(String backendName, SupportsNetworkInterfaces...vms);
        }

        /**
         * The stage of the load balancer definition allowing to add a public ip address to the load
         * balancer's front end.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPublicIpAddresses<ReturnT> {
            /**
             * Sets the provided set of public IP addresses as the front end for the load balancer, making it an Internet-facing load balancer.
             * @param publicIpAddresses existing public IP addresses
             * @return the next stage of the resource definition
             */
            ReturnT withExistingPublicIpAddresses(PublicIpAddress...publicIpAddresses);

            /**
             * Adds a new public IP address to the front end of the load balancer, using an automatically generated name and leaf DNS label
             * derived from the load balancer's name, in the same resource group and region.
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIpAddress();

            /**
             * Adds a new public IP address to the front end of the load balancer, using the specified DNS leaft label,
             * an automatically generated name derived from the DNS label, in the same resource group and region.
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIpAddress(String dnsLeafLabel);

            /**
             * Adds a new public IP address to the front end of the load balancer, creating the public IP based on the provided {@link Creatable}
             * stage of a public IP endpoint's definition.
             *
             * @return the next stage of the definition
             */
            ReturnT withNewPublicIpAddress(Creatable<PublicIpAddress> creatablePublicIpAddress);
        }

        /**
         * The stage of the load balancer definition allowing to create a load balancing rule.
         */
        interface WithLoadBalancingRule {
            /**
             * Creates a load balancing rule between the specified front end and back end ports and protocol.
             * @param frontendPort the port number on the front end to accept incoming traffic on
             * @param protocol the protocol to load balance
             * @param backendPort the port number on the back end to send load balanced traffic to
             * @param name the name for the load balancing rule
             * @return the next stage of the definition
             */
            WithCreateAndRule withLoadBalancingRule(int frontendPort, TransportProtocol protocol, int backendPort, String name);

            /**
             * Creates a load balancing rule between the specified front end and back end ports and protocol.
             * <p>
             * The new rule will be assigned an automatically generated name.
             * @param frontendPort the port number on the front end to accept incoming traffic on
             * @param protocol the protocol to load balance
             * @param backendPort the port number on the back end to send load balanced traffic to
             * @return the next stage of the definition
             */
            WithCreateAndRule withLoadBalancingRule(int frontendPort, TransportProtocol protocol, int backendPort);

            /**
             * Creates a load balancing rule for the specified port and protocol.
             * @param port the port number on the front and back end for the network traffic to be load balanced on
             * @param protocol the protocol to load balance
             * @return the next stage of the definition
             */
            WithCreateAndRule withLoadBalancingRule(int port, TransportProtocol protocol);

            /**
             * Creates a load balancing rule for the specified port and protocol.
             * @param port the port number on the front and back ends for the network traffic to be load balanced on
             * @param protocol the protocol to load balance
             * @param name a unique name for the load balancing rule
             * @return the next stage of the definition
             */
            WithCreateAndRule withLoadBalancingRule(int port, TransportProtocol protocol, String name);

            /**
             * Begins the definition of a new load balancing rule to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link TcpProbe.DefinitionStages.WithAttach#attach()}
             * @param name the name of the load balancing rule
             * @return the first stage of the new load balancing rule definition
             */
            LoadBalancingRule.DefinitionStages.Blank<WithCreateAndRule> defineLoadBalancingRule(String name);
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
            WithCreateAndRule withTcpProbe(int port);

            /**
             * Adds a TCP probe checking the specified port.
             * <p>
             * An automatically generated name is assigned to the probe.
             * @param port the port number for the probe to monitor
             * @param name the name for the probe, so that the probe can be referenced from load balancing rules
             * @return the next stage of the definition
             */
            WithCreateAndRule withTcpProbe(int port, String name);

            /**
             * Adds an HTTP probe checking for an HTTP 200 response from the specified path at regular intervals, using port 80.
             * <p>
             * An automatically generated name is assigned to the probe.
             * @param requestPath the path for the probe to invoke
             * @return the next stage of the definition
             */
            WithCreateAndRule withHttpProbe(String requestPath);

            /**
             * Adds an HTTP probe checking for an HTTP 200 response from the specified path at regular intervals, using port 80.
             * @param requestPath the path for the probe to invoke
             * @param name the name to assign to the probe so that references to the probe can be made from load balancing rules
             * @return the next stage of the definition
             */
            WithCreateAndRule withHttpProbe(String requestPath, String name);

            /**
             * Begins the definition of a new TCP probe to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link TcpProbe.DefinitionStages.WithAttach#attach()}
             * @param name the name of the probe
             * @return the first stage of the new probe definition
             */
            TcpProbe.DefinitionStages.Blank<WithCreate> defineTcpProbe(String name);

            /**
             * Begins the definition of a new HTTP probe to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link HttpProbe.DefinitionStages.WithAttach#attach()}
             * @param name the name of the probe
             * @return the first stage of the new probe definition
             */
            HttpProbe.DefinitionStages.Blank<WithCreate> defineHttpProbe(String name);
        }


        /**
         * The stage of the load balancer definition containing all the required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allowing
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<LoadBalancer>,
            Resource.DefinitionWithTags<WithCreate>,
            WithProbe {
        }

        /**
         * The stage of the load balancer definition containing all the required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allowing
         * for any other optional settings to be specified, including load balancing rules.
         */
        interface WithCreateAndRule extends
            WithCreate,
            WithLoadBalancingRule {
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
             * Adds a new empty backend to the load balancer.
             * @param name the name to assign to the backend
             * @return the next stage of the update
             */
            Update withBackend(String name);

            /**
             * Removes the specified backend from the load balancer.
             * @param name the name of the backend to remove
             * @return the next stage of the update
             */
            Update withoutBackend(String name);
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
             * Adds a TCP probe checking the specified port.
             * <p>
             * An automatically generated name is assigned to the probe.
             * @param port the port number for the probe to monitor
             * @param name the name for the probe, so that the probe can be referenced from load balancing rules
             * @return the next stage of the definition
             */
            Update withTcpProbe(int port, String name);

            /**
             * Adds an HTTP probe checking for an HTTP 200 response from the specified path at regular intervals, using port 80.
             * <p>
             * An automatically generated name is assigned to the probe.
             * @param requestPath the path for the probe to invoke
             * @return the next stage of the definition
             */
            Update withHttpProbe(String requestPath);

            /**
             * Adds an HTTP probe checking for an HTTP 200 response from the specified path at regular intervals, using port 80.
             * @param requestPath the path for the probe to invoke
             * @param name the name to assign to the probe so that references to the probe can be made from load balancing rules
             * @return the next stage of the definition
             */
            Update withHttpProbe(String requestPath, String name);

            /**
             * Begins the definition of a new HTTP probe to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link HttpProbe.DefinitionStages.WithAttach#attach()}
             * @param name the name of the new probe
             * @return the next stage of the definition
             */
            HttpProbe.UpdateDefinitionStages.Blank<Update> defineHttpProbe(String name);

            /**
             * Begins the definition of a new TCP probe to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link HttpProbe.DefinitionStages.WithAttach#attach()}
             * @param name the name of the new probe
             * @return the next stage of the definition
             */
            TcpProbe.UpdateDefinitionStages.Blank<Update> defineTcpProbe(String name);

            /**
             * Removes the specified probe from the load balancer, if present.
             * @param name the name of the probe to remove
             * @return the next stage of the update
             */
            Update withoutProbe(String name);

            /**
             * Removes the specified probe from the load balancer, if present.
             * @param probe the probe to remove
             * @return the next stage of the update
             */
            Update withoutProbe(Probe probe);

            /**
             * Begins the description of an update to an existing TCP probe on this load balancer.
             * @param name the name of the probe to update
             * @return the first stage of the probe update
             */
            TcpProbe.Update updateTcpProbe(String name);

            /**
             * Begins the description of an update to an existing HTTP probe on this load balancer.
             * @param name the name of the probe to update
             * @return the first stage of the probe update
             */
            HttpProbe.Update updateHttpProbe(String name);
        }

        /**
         * The stage of the load balancer update allowing to add, remove or modify load balancing rules.
         */
        interface WithLoadBalancingRule {
            /**
             * Creates a load balancing rule between the specified front end and back end ports and protocol.
             * @param frontendPort the port number on the front end to accept incoming traffic on
             * @param protocol the protocol to load balance
             * @param backendPort the port number on the back end to send load balanced traffic to
             * @param name the name for the load balancing rule
             * @return the next stage of the definition
             */
            Update withLoadBalancingRule(int frontendPort, TransportProtocol protocol, int backendPort, String name);

            /**
             * Creates a load balancing rule between the specified front end and back end ports and protocol.
             * <p>
             * The new rule will be assigned an automatically generated name.
             * @param frontendPort the port number on the front end to accept incoming traffic on
             * @param protocol the protocol to load balance
             * @param backendPort the port number on the back end to send load balanced traffic to
             * @return the next stage of the definition
             */
            Update withLoadBalancingRule(int frontendPort, TransportProtocol protocol, int backendPort);

            /**
             * Creates a load balancing rule for the specified port and protocol.
             * @param port the port number on the front and back end for the network traffic to be load balanced on
             * @param protocol the protocol to load balance
             * @return the next stage of the definition
             */
            Update withLoadBalancingRule(int port, TransportProtocol protocol);

            /**
             * Begins the definition of a new load balancing rule to add to the load balancer.
             * <p>
             * The definition must be completed with a call to {@link TcpProbe.DefinitionStages.WithAttach#attach()}
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
             * Removes the specified load balancing rule from the load balancer, if present.
             * @param rule the load balancing rule to remove
             * @return the next stage of the update
             */
            Update withoutLoadBalancingRule(LoadBalancingRule rule);

            /**
             * Begins the description of an update to an existing load balancing rule on this load balancer.
             * @param name the name of the load balancing rule to update
             * @return the first stage of the load balancing rule update
             */
            LoadBalancingRule.Update updateLoadBalancingRule(String name);
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
        UpdateStages.WithLoadBalancingRule {
    }
}
