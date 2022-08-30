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

    /**
     * @return the tags of the agents.
     */
    Map<String, String> tags();
}
