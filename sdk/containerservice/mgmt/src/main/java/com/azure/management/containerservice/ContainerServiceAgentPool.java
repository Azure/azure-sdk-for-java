/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerservice;


import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.azure.management.resources.fluentcore.model.Attachable;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * A client-side representation for a container service agent pool.
 */
@Fluent
public interface ContainerServiceAgentPool extends
        ChildResource<OrchestratorServiceBase>,
        HasInner<ContainerServiceAgentPoolProfile> {

    /**
     * @return the number of agents (virtual machines) to host docker containers
     */
    int count();

    /**
     * @return the size of each virtual machine in the agent pool
     */
    ContainerServiceVMSizeTypes vmSize();

    /**
     * @return DNS prefix to be used to create the FQDN for the agent pool
     */
    String dnsPrefix();

    /**
     * @return FDQN for the agent pool
     */
    String fqdn();

    /**
     * @return OS disk size in GB set for each virtual machine in the agent pool
     */
    int osDiskSizeInGB();

    /**
     * @return array of ports opened on this agent pool
     */
    int[] ports();

    /**
     * @return OS of each virtual machine in the agent pool
     */
    OSType osType();

    /**
     * @return the storage kind (managed or classic) set for each virtual machine in the agent pool
     */
    ContainerServiceStorageProfileTypes storageProfile();

    /**
     * @return the name of the subnet used by each virtual machine in the agent pool
     */
    String subnetName();

    /**
     * @return the ID of the virtual network used by each virtual machine in the agent pool
     */
    String networkId();


    // Fluent interfaces

    /**
     * The entirety of a container service agent pool definition as a part of a parent definition.
     * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithVMSize<ParentT>,
        DefinitionStages.WithLeafDomainLabel<ParentT> {
    }

    /**
     * Grouping of container service agent pool definition stages as a part of parent container service definition.
     */
    interface DefinitionStages {

        /**
         * The first stage of a container service agent pool definition.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface Blank<ParentT> {
            /**
             * Specifies the number of agents (virtual machines) to host docker containers.
             *
             * @param count a number between 1 and 100
             * @return the next stage of the definition
             */
            WithVMSize<ParentT> withVirtualMachineCount(int count);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent virtual machine size.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface WithVMSize<ParentT> {
            /**
             * Specifies the size of the agent virtual machines.
             * @param vmSize the size of the virtual machine
             * @return the next stage of the definition
             */
            WithLeafDomainLabel<ParentT> withVirtualMachineSize(ContainerServiceVMSizeTypes vmSize);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the DNS prefix.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface WithLeafDomainLabel<ParentT> {
            /**
             * Specify the DNS prefix to be used in the FQDN for the agent pool.
             * @param dnsPrefix the DNS prefix
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDnsPrefix(String dnsPrefix);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent pool ports to be exposed.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface WithPorts<ParentT> {
            /**
             * Ports to be exposed on this agent pool.
             *<p>
             * The default exposed ports are different based on your choice of orchestrator.
             * @param ports port numbers that will be exposed on this agent pool
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPorts(int... ports);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent pool OS type.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface WithOSType<ParentT> {
            /**
             * OS type to be used for every machine in the agent pool.
             *
             * Default is Linux.
             * @param osType OS type to be used for every machine in the agent pool
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOSType(OSType osType);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent pool OS disk size.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface WithOSDiskSize<ParentT> {
            /**
             * OS Disk Size in GB to be used for every machine in the agent pool.
             *
             * @param osDiskSizeInGB OS disk size in GB to be used for each virtual machine in the agent pool
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOSDiskSizeInGB(int osDiskSizeInGB);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent pool storage kind.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface WithStorageProfile<ParentT> {
            /**
             * Specifies the storage kind to be used for each virtual machine in the agent pool.
             *
             * @param storageProfile the storage kind to be used for each virtual machine in the agent pool
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withStorageProfile(ContainerServiceStorageProfileTypes storageProfile);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify a virtual network to be used for the agents.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface WithVirtualNetwork<ParentT> {
            /**
             * Specifies the virtual network to be used for the agents.
             *
             * @param virtualNetworkId the ID of a virtual network
             * @param subnetName the name of the subnet within the virtual network.; the subnet must have the service
             *                   endpoints enabled for 'Microsoft.ContainerService'.
             * @return the next stage
             */
            WithAttach<ParentT> withVirtualNetwork(String virtualNetworkId, String subnetName);
        }

        /** The final stage of a container service agent pool definition.
         * At this stage, any remaining optional settings can be specified, or the container service agent pool
         * can be attached to the parent container service definition.
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            WithOSType<ParentT>,
            WithOSDiskSize<ParentT>,
            WithPorts<ParentT>,
            WithStorageProfile<ParentT>,
            WithVirtualNetwork<ParentT>,
            Attachable.InDefinition<ParentT> {
        }

    }
}
