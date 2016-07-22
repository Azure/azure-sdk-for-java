/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.List;

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
     * The entirety of the load balancer definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithVirtualMachine,
        DefinitionStages.WithCreate,
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
         * The stage of the definition describing the nature of the frontend of the load balancer: imternal or Internet-facing.
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
            // TODO defineFrontend(String name)...
        }

        /**
         * The stage of the definition allowing to add additional Internet-facing frontends or add the first back end pool.
         */
        interface WithInternetFrontendOrBackend extends WithInternetFrontends, WithBackend {
        }

        /**
         * The stage of the definition allowing to add a backend pool.
         */
        interface WithBackend extends WithVirtualMachine {
        }

        /**
         * The stage of the definition allowing to add another backend pool or the first load balancing rule.
         */
        interface WithBackendOrCreate extends WithBackend, WithCreate {
            // TODO
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
             * @param vms existing virtual machines
             * @return the next stage of the update
             */
            WithBackendOrCreate withExistingVirtualMachines(SupportsNetworkInterfaces...vms);
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
        interface WithLoadBalancingRules {
            /**
             * Creates a load balancing rule between the specified front end and back end ports for the specified protocol.
             * @param protocol the network protocol for the rule
             * @param frontendPort the port number on the front end to accept incoming traffic on
             * @param backendPort the port number on the back end to send load balanced traffic to
             * @param name the name for the load balancing rule
             * @return the next stage of the definition
             */
            WithCreate withLoadBalancingRule(Protocol protocol, int frontendPort, int backendPort, String name);

            /**
             * Creates a load balancing rule between the specified front end and back end ports for the specified protocol.
             * <p>
             * The new rule will be assigned an automatically generated name.
             * @param protocol the network protocol for the rule
             * @param frontendPort the port number on the front end to accept incoming traffic on
             * @param backendPort the port number on the back end to send load balanced traffic to
             * @return the next stage of the definition
             */
            WithCreate withLoadBalancingRule(Protocol protocol, int frontendPort, int backendPort);

            /**
             * Creates a load balancing rule for the specified port and protocol.
             * @param protocol the network protocol for the rule
             * @param port the port number on the front and back end for the network traffic to be load balanced on
             * @return the next stage of the definition
             */
            WithCreate withLoadBalancingRule(Protocol protocol, int port);
        }

        /**
         * The stage of the load balancer definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<LoadBalancer>,
            Resource.DefinitionWithTags<WithCreate>,
            WithLoadBalancingRules {
       }
    }

    /**
     * Grouping of load balancer update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for a load balancer update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<LoadBalancer>,
        Resource.UpdateWithTags<Update> {
    }
}
