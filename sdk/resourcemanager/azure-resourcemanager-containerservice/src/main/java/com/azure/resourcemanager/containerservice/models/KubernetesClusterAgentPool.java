// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

import java.util.List;
import java.util.Map;

/** A client-side representation for a Kubernetes cluster agent pool. */
@Fluent
public interface KubernetesClusterAgentPool
    extends ChildResource<KubernetesCluster>, HasInnerModel<ManagedClusterAgentPoolProfile> {

    /** @return the provisioning state of the agent pool */
    String provisioningState();

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

    /** @return agent pool mode */
    AgentPoolMode mode();

    /** @return the name of the subnet used by each virtual machine in the agent pool */
    String subnetName();

    /** @return the ID of the virtual network used by each virtual machine in the agent pool */
    String networkId();

    /** @return the list of availability zones */
    List<String> availabilityZones();

    /** @return the map of node labels */
    Map<String, String> nodeLabels();

    /** @return the list of node taints */
    List<String> nodeTaints();

    /** @return the power state, Running or Stopped */
    PowerState powerState();

    /** @return the number of agents (VMs) to host docker containers */
    int nodeSize();

    /** @return the maximum number of pods per node */
    int maximumPodsPerNode();

    /** @return whether auto-scaling is enabled */
    boolean isAutoScalingEnabled();

    /** @return the minimum number of nodes for auto-scaling */
    int minimumNodeSize();

    /** @return the maximum number of nodes for auto-scaling */
    int maximumNodeSize();

    /** @return the priority of each virtual machines in the agent pool */
    ScaleSetPriority virtualMachinePriority();

    /** @return the eviction policy of each virtual machines in the agent pool */
    ScaleSetEvictionPolicy virtualMachineEvictionPolicy();

    /** @return the maximum price of each spot virtual machines in the agent pool, -1 means pay-as-you-go prices */
    Double virtualMachineMaximumPrice();

    /**
     * @return the OS disk type to be used for machines in the agent pool
     */
    OSDiskType osDiskType();

    /**
     * @return the disk type for the placement of emptyDir volumes, container runtime data root,
     * and Kubelet ephemeral storage
     */
    KubeletDiskType kubeletDiskType();

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
            WithAgentPoolVirtualMachineCount<ParentT> withVirtualMachineSize(ContainerServiceVMSizeTypes vmSize);
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
         * The stage of a container service agent pool definition allowing to specify auto-scaling.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithAutoScaling<ParentT> {
            /**
             * Enables the auto-scaling with maximum/minimum number of nodes.
             *
             * @param minimumNodeSize the minimum number of nodes for auto-scaling.
             * @param maximumNodeSize the maximum number of nodes for auto-scaling.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAutoScaling(int minimumNodeSize, int maximumNodeSize);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify availability zones.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithAvailabilityZones<ParentT> {
            /**
             * Specifies the availability zones.
             *
             * @param zones the availability zones, can be 1, 2, 3.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAvailabilityZones(Integer... zones);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify node labels and taints.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithNodeLabelsTaints<ParentT> {
            /**
             * Specifies the node labels for all nodes.
             *
             * @param nodeLabels the node labels.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withNodeLabels(Map<String, String> nodeLabels);

            /**
             * Specifies the node labels.
             *
             * @param nodeTaints the node taints for new nodes.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withNodeTaints(List<String> nodeTaints);
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
         * The stage of a container service agent pool definition allowing to specify the priority of the virtual
         * machine.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithVMPriority<ParentT> {
            /**
             * Specifies the priority of the virtual machines.
             *
             * @param priority the priority
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVirtualMachinePriority(ScaleSetPriority priority);

            /**
             * Specify that virtual machines should be spot priority VMs.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSpotPriorityVirtualMachine();

            /**
             * Specify that virtual machines should be spot priority VMs with provided eviction policy.
             *
             * @param policy eviction policy for the virtual machines.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSpotPriorityVirtualMachine(ScaleSetEvictionPolicy policy);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent pool mode.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithBillingProfile<ParentT> {
            /**
             * Sets the maximum price for virtual machine in agent pool. This price is in US Dollars.
             *
             * Default is -1 if not specified, as up to pay-as-you-go prices.
             *
             * @param maxPriceInUsDollars the maximum price in US Dollars
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withVirtualMachineMaximumPrice(Double maxPriceInUsDollars);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent pool disk type.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithDiskType<ParentT> {
            /**
             * The OS disk type to be used for machines in the agent pool.
             *
             * The default is 'Ephemeral' if the VM supports it and has a cache disk larger than the requested
             * OSDiskSizeGB. Otherwise, defaults to 'Managed'.
             *
             * @param osDiskType the OS disk type to be used for machines in the agent pool
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withOSDiskType(OSDiskType osDiskType);

            /**
             * The disk type for the placement of emptyDir volumes, container runtime data root,
             * and Kubelet ephemeral storage.
             *
             * @param kubeletDiskType the disk type for the placement of emptyDir volumes, container runtime data root,
             *                        and Kubelet ephemeral storage.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withKubeletDiskType(KubeletDiskType kubeletDiskType);
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
                WithAutoScaling<ParentT>,
                WithAvailabilityZones<ParentT>,
                WithNodeLabelsTaints<ParentT>,
                WithVMPriority<ParentT>,
                WithBillingProfile<ParentT>,
                WithDiskType<ParentT>,
                Attachable.InDefinition<ParentT> {
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update<ParentT>
        extends Settable<ParentT>,
            UpdateStages.WithAgentPoolVirtualMachineCount<ParentT>,
            UpdateStages.WithAutoScaling<ParentT>,
            UpdateStages.WithAgentPoolMode<ParentT> { }

    /** Grouping of agent pool update stages. */
    interface UpdateStages {
        /**
         * The stage of a container service agent pool update allowing to specify the number of agents (Virtual
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
             * @return the next stage of the update
             */
            Update<ParentT> withAgentPoolVirtualMachineCount(int count);
        }

        /**
         * The stage of a container service agent pool update allowing to specify the agent pool mode.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithAgentPoolMode<ParentT> {
            /**
             * Specifies the agent pool mode for the agents.
             *
             * @param agentPoolMode the agent pool mode
             * @return the next stage of the update
             */
            Update<ParentT> withAgentPoolMode(AgentPoolMode agentPoolMode);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify auto-scaling.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithAutoScaling<ParentT> {
            /**
             * Enables the auto-scaling with maximum/minimum number of nodes.
             *
             * @param minimumNodeCount the minimum number of nodes for auto-scaling.
             * @param maximumNodeCount the maximum number of nodes for auto-scaling.
             * @return the next stage of the update
             */
            Update<ParentT> withAutoScaling(int minimumNodeCount, int maximumNodeCount);

            /**
             * Disables the auto-scaling with maximum/minimum number of nodes.
             *
             * @return the next stage of the update
             */
            Update<ParentT> withoutAutoScaling();
        }
    }
}
