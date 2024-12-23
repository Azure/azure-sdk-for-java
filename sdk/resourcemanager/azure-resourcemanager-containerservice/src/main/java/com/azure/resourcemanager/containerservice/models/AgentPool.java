// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice.models;

import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;

import java.util.List;
import java.util.Map;

/**
 * A client-side representation for an agent pool.
 */
public interface AgentPool extends HasName {

    /**
     * Gets the provisioning state of the agent pool.
     *
     * @return the provisioning state of the agent pool
     */
    String provisioningState();

    /**
     * Gets the number of agents (virtual machines) to host docker containers.
     *
     * @return the number of agents (virtual machines) to host docker containers
     */
    int count();

    /**
     * Gets size of each agent virtual machine in the agent pool.
     *
     * @return size of each agent virtual machine in the agent pool
     */
    ContainerServiceVMSizeTypes vmSize();

    /**
     * Gets OS disk size in GB set for each virtual machine in the agent pool.
     *
     * @return OS disk size in GB set for each virtual machine in the agent pool
     */
    int osDiskSizeInGB();

    /**
     * Gets OS of each virtual machine in the agent pool.
     *
     * @return OS of each virtual machine in the agent pool
     */
    OSType osType();

    /**
     * Gets agent pool type.
     *
     * @return agent pool type
     */
    AgentPoolType type();

    /**
     * Gets agent pool mode.
     *
     * @return agent pool mode
     */
    AgentPoolMode mode();

    /**
     * Gets the name of the subnet used by each virtual machine in the agent pool.
     *
     * @return the name of the subnet used by each virtual machine in the agent pool
     */
    String subnetName();

    /**
     * Gets the ID of the virtual network used by each virtual machine in the agent pool.
     *
     * @return the ID of the virtual network used by each virtual machine in the agent pool
     */
    String networkId();

    /**
     * Gets the list of availability zones.
     *
     * @return the list of availability zones
     */
    List<String> availabilityZones();

    /**
     * Gets the map of node labels.
     *
     * @return the map of node labels
     */
    Map<String, String> nodeLabels();

    /**
     * Gets the list of node taints.
     *
     * @return the list of node taints
     */
    List<String> nodeTaints();

    /**
     * Gets the power state.
     *
     * @return the power state, Running or Stopped
     */
    PowerState powerState();

    /**
     * Gets the number of agents (VMs) to host docker containers.
     *
     * @return the number of agents (VMs) to host docker containers
     */
    int nodeSize();

    /**
     * Gets the maximum number of pods per node.
     *
     * @return the maximum number of pods per node
     */
    int maximumPodsPerNode();

    /**
     * Checks whether auto-scaling is enabled.
     *
     * @return whether auto-scaling is enabled
     */
    boolean isAutoScalingEnabled();

    /**
     * Gets the minimum number of nodes for auto-scaling.
     *
     * @return the minimum number of nodes for auto-scaling
     */
    int minimumNodeSize();

    /**
     * Gets the maximum number of nodes for auto-scaling.
     *
     * @return the maximum number of nodes for auto-scaling
     */
    int maximumNodeSize();

    /**
     * Gets the priority of each virtual machines in the agent pool.
     *
     * @return the priority of each virtual machines in the agent pool
     */
    ScaleSetPriority virtualMachinePriority();

    /**
     * Gets the eviction policy of each virtual machines in the agent pool.
     *
     * @return the eviction policy of each virtual machines in the agent pool
     */
    ScaleSetEvictionPolicy virtualMachineEvictionPolicy();

    /**
     * Gets the maximum price of each spot virtual machines in the agent pool.
     *
     * @return the maximum price of each spot virtual machines in the agent pool, -1 means pay-as-you-go prices
     */
    Double virtualMachineMaximumPrice();

    /**
     * Gets the maximum price of each spot virtual machines in the agent pool.
     *
     * @return the OS disk type to be used for machines in the agent pool
     */
    OSDiskType osDiskType();

    /**
     * Gets the disk type for the placement.
     *
     * @return the disk type for the placement of emptyDir volumes, container runtime data root,
     * and Kubelet ephemeral storage
     */
    KubeletDiskType kubeletDiskType();

    /**
     * Gets the tags of the agents.
     *
     * @return the tags of the agents.
     */
    Map<String, String> tags();

    /**
     * Checks whether FIPS-enabled OS is being used for agent pool's machines.
     *
     * @return whether FIPS-enabled OS is being used for agent pool's machines
     * @see <a href="https://docs.microsoft.com/azure/aks/use-multiple-node-pools#add-a-fips-enabled-node-pool-preview">
     *     Add a FIPS-enabled node pool</a> for more details.
     */
    boolean isFipsEnabled();
}
