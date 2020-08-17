// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** A client-side representation for a Kubernetes cluster agent pool. */
@Fluent
public interface KubernetesClusterAgentPool
    extends ChildResource<OrchestratorServiceBase>, HasInner<ManagedClusterAgentPoolProfile> {

    /** @return the number of agents (virtual machines) to host docker containers */
    int count();

    /** @return size of each agent virtual machine in the agent pool */
    ContainerServiceVMSizeTypes vmSize();

    /** @return OS disk size in GB set for each virtual machine in the agent pool */
    int osDiskSizeInGB();

    /** @return OS of each virtual machine in the agent pool */
    OSType osType();

    /** @return agent pool type */
    AgentPoolType type();

    /** @return the name of the subnet used by each virtual machine in the agent pool */
    String subnetName();

    /** @return the ID of the virtual network used by each virtual machine in the agent pool */
    String networkId();

    // Fluent interfaces

    /**
     * The entirety of a container service agent pool definition as a part of a parent definition.
     *
     * @param <ParentT> the stage of the container service definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithOSType<ParentT>,
            DefinitionStages.WithOSDiskSize<ParentT>,
            DefinitionStages.WithAgentPoolType<ParentT>,
            DefinitionStages.WithAgentPoolVirtualMachineCount<ParentT>,
            DefinitionStages.WithMaxPodsCount<ParentT>,
            DefinitionStages.WithVirtualNetwork<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of container service agent pool definition stages as a part of parent container service definition. */
    interface DefinitionStages {

        /**
         * The first stage of a container service agent pool definition allowing to specify the agent virtual machine
         * size.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface Blank<ParentT> {
            /**
             * Specifies the size of the virtual machines to be used as agents.
             *
             * @param vmSize the size of each virtual machine in the agent pool
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVirtualMachineSize(ContainerServiceVMSizeTypes vmSize);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent pool OS type.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithOSType<ParentT> {
            /**
             * OS type to be used for each virtual machine in the agent pool.
             *
             * <p>Default is Linux.
             *
             * @param osType OS type to be used for each virtual machine in the agent pool
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOSType(OSType osType);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent pool OS disk size.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithOSDiskSize<ParentT> {
            /**
             * OS disk size in GB to be used for each virtual machine in the agent pool.
             *
             * @param osDiskSizeInGB OS Disk Size in GB to be used for every machine in the agent pool
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOSDiskSizeInGB(int osDiskSizeInGB);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the type of agent pool. Allowed
         * values could be seen in AgentPoolType Class.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithAgentPoolType<ParentT> {
            /**
             * Set agent pool type to every virtual machine in the agent pool.
             *
             * @param agentPoolType the agent pool type for every machine in the agent pool
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAgentPoolType(AgentPoolType agentPoolType);

            /**
             * Set agent pool type by type name.
             *
             * @param agentPoolTypeName the agent pool type name in string format
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAgentPoolTypeName(String agentPoolTypeName);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the number of agents (Virtual
         * Machines) to host docker containers.
         *
         * <p>Allowed values must be in the range of 1 to 100 (inclusive); the default value is 1.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithAgentPoolVirtualMachineCount<ParentT> {
            /**
             * Specifies the number of agents (Virtual Machines) to host docker containers.
             *
             * @param count the number of agents (VMs) to host docker containers. Allowed values must be in the range of
             *     1 to 100 (inclusive); the default value is 1.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAgentPoolVirtualMachineCount(int count);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the maximum number of pods that
         * can run on a node.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithMaxPodsCount<ParentT> {
            /**
             * Specifies the maximum number of pods that can run on a node.
             *
             * @param podsCount the maximum number of pods that can run on a node
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMaxPodsCount(int podsCount);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify a virtual network to be used for
         * the agents.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithVirtualNetwork<ParentT> {
            /**
             * Specifies the virtual network to be used for the agents.
             *
             * @param virtualNetworkId the ID of a virtual network
             * @param subnetName the name of the subnet within the virtual network.; the subnet must have the service
             *     endpoints enabled for 'Microsoft.ContainerService'.
             * @return the next stage
             */
            WithAttach<ParentT> withVirtualNetwork(String virtualNetworkId, String subnetName);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent pool mode.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithAgentPoolMode<ParentT> {
            /**
             * Specifies the agent pool mode for the agents.
             *
             * @param agentPoolMode the agent pool mode
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAgentPoolMode(AgentPoolMode agentPoolMode);
        }

        /**
         * The final stage of a container service agent pool definition. At this stage, any remaining optional settings
         * can be specified, or the container service agent pool can be attached to the parent container service
         * definition.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends WithOSType<ParentT>,
                WithOSDiskSize<ParentT>,
                WithAgentPoolType<ParentT>,
                WithAgentPoolVirtualMachineCount<ParentT>,
                WithMaxPodsCount<ParentT>,
                WithVirtualNetwork<ParentT>,
                WithAgentPoolMode<ParentT>,
                Attachable.InDefinition<ParentT> {
        }
    }
}
