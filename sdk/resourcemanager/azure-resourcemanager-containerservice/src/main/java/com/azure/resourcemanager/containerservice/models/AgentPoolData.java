// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice.models;

import com.azure.resourcemanager.containerservice.fluent.models.AgentPoolInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The client-side data of an agent pool.
 */
public class AgentPoolData implements AgentPool, HasInnerModel<AgentPoolInner> {

    private final AgentPoolInner innerModel;

    /**
     * Creates an instance of agent pool data.
     */
    public AgentPoolData() {
        this(new AgentPoolInner());
    }

    /**
     * Creates an instance of agent pool data.
     *
     * @param innerModel the inner model of agent pool.
     */
    protected AgentPoolData(AgentPoolInner innerModel) {
        this.innerModel = innerModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return innerModel().name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provisioningState() {
        return this.innerModel().provisioningState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int count() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().count());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerServiceVMSizeTypes vmSize() {
        return ContainerServiceVMSizeTypes.fromString(this.innerModel().vmSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int osDiskSizeInGB() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().osDiskSizeGB());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OSType osType() {
        return this.innerModel().osType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentPoolType type() {
        return this.innerModel().typePropertiesType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentPoolMode mode() {
        return this.innerModel().mode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String subnetName() {
        return ResourceUtils.nameFromResourceId(this.innerModel().vnetSubnetId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String networkId() {
        String subnetId = (this.innerModel() != null) ? this.innerModel().vnetSubnetId() : null;
        return (subnetId != null) ? ResourceUtils.parentResourceIdFromResourceId(subnetId) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> availabilityZones() {
        return innerModel().availabilityZones();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> nodeLabels() {
        return innerModel().nodeLabels() == null ? null : Collections.unmodifiableMap(innerModel().nodeLabels());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> nodeTaints() {
        return innerModel().nodeTaints() == null ? null : Collections.unmodifiableList(innerModel().nodeTaints());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PowerState powerState() {
        return innerModel().powerState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int nodeSize() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().count());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int maximumPodsPerNode() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().maxPods());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutoScalingEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().enableAutoScaling());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int minimumNodeSize() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().minCount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int maximumNodeSize() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().maxCount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScaleSetPriority virtualMachinePriority() {
        return innerModel().scaleSetPriority();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScaleSetEvictionPolicy virtualMachineEvictionPolicy() {
        return innerModel().scaleSetEvictionPolicy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double virtualMachineMaximumPrice() {
        return innerModel().spotMaxPrice().doubleValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OSDiskType osDiskType() {
        return innerModel().osDiskType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KubeletDiskType kubeletDiskType() {
        return innerModel().kubeletDiskType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFipsEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().enableFips());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> tags() {
        return innerModel().tags();
    }

    /**
     * Specifies the size of the virtual machines to be used as agents.
     *
     * @param vmSize the size of each virtual machine in the agent pool
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withVirtualMachineSize(ContainerServiceVMSizeTypes vmSize) {
        this.innerModel().withVmSize(vmSize.toString());
        return this;
    }

    /**
     * Specifies OS type to be used for each virtual machine in the agent pool.
     *
     * @param osType OS type to be used for each virtual machine in the agent pool
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withOSType(OSType osType) {
        this.innerModel().withOsType(osType);
        return this;
    }

    /**
     * Specifies OS disk size in GB to be used for each virtual machine in the agent pool.
     *
     * @param osDiskSizeInGB OS Disk Size in GB to be used for every machine in the agent pool
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withOSDiskSizeInGB(int osDiskSizeInGB) {
        this.innerModel().withOsDiskSizeGB(osDiskSizeInGB);
        return this;
    }

    /**
     * Set agent pool type to every virtual machine in the agent pool.
     *
     * @param agentPoolType the agent pool type for every machine in the agent pool
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withAgentPoolType(AgentPoolType agentPoolType) {
        this.innerModel().withTypePropertiesType(agentPoolType);
        return this;
    }

    /**
     * Set agent pool type by type name.
     *
     * @param agentPoolTypeName the agent pool type name in string format
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withAgentPoolTypeName(String agentPoolTypeName) {
        this.innerModel().withTypePropertiesType(AgentPoolType.fromString(agentPoolTypeName));
        return this;
    }

    /**
     * Specifies the number of agents (Virtual Machines) to host docker containers.
     *
     * @param count the number of agents (VMs) to host docker containers. Allowed values must be in the range of
     *              1 to 100 (inclusive); the default value is 1.
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withAgentPoolVirtualMachineCount(int count) {
        this.innerModel().withCount(count);
        return this;
    }

    /**
     * Specifies the maximum number of pods that can run on a node.
     *
     * @param podsCount the maximum number of pods that can run on a node
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withMaxPodsCount(int podsCount) {
        this.innerModel().withMaxPods(podsCount);
        return this;
    }

    /**
     * Specifies the virtual network to be used for the agents.
     *
     * @param virtualNetworkId the ID of a virtual network
     * @param subnetName the name of the subnet within the virtual network.; the subnet must have the service
     *                   endpoints enabled for 'Microsoft.ContainerService'.
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withVirtualNetwork(String virtualNetworkId, String subnetName) {
        String vnetSubnetId = virtualNetworkId + "/subnets/" + subnetName;
        this.innerModel().withVnetSubnetId(vnetSubnetId);
        return this;
    }

    /**
     * Specifies the agent pool mode for the agents.
     *
     * @param agentPoolMode the agent pool mode
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withAgentPoolMode(AgentPoolMode agentPoolMode) {
        innerModel().withMode(agentPoolMode);
        return this;
    }

    /**
     * Enables the auto-scaling with maximum/minimum number of nodes.
     *
     * @param minimumNodeSize the minimum number of nodes for auto-scaling.
     * @param maximumNodeSize the maximum number of nodes for auto-scaling.
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withAutoScaling(int minimumNodeSize, int maximumNodeSize) {
        innerModel().withEnableAutoScaling(true);
        innerModel().withMinCount(minimumNodeSize);
        innerModel().withMaxCount(maximumNodeSize);
        return this;
    }

    /**
     * Specifies the availability zones.
     *
     * @param zones the availability zones, can be 1, 2, 3.
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withAvailabilityZones(Integer... zones) {
        innerModel().withAvailabilityZones(Arrays.stream(zones).map(String::valueOf).collect(Collectors.toList()));
        return this;
    }

    /**
     * Specifies the node labels for all nodes.
     *
     * @param nodeLabels the node labels.
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withNodeLabels(Map<String, String> nodeLabels) {
        innerModel().withNodeLabels(nodeLabels == null ? null : new TreeMap<>(nodeLabels));
        return this;
    }

    /**
     * Specifies the node labels.
     *
     * @param nodeTaints the node taints for new nodes.
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withNodeTaints(List<String> nodeTaints) {
        innerModel().withNodeTaints(nodeTaints);
        return this;
    }

    /**
     * Specifies the priority of the virtual machines.
     *
     * @param priority the priority
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withVirtualMachinePriority(ScaleSetPriority priority) {
        innerModel().withScaleSetPriority(priority);
        return this;
    }

    /**
     * Specify that virtual machines should be spot priority VMs.
     *
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withSpotPriorityVirtualMachine() {
        innerModel().withScaleSetPriority(ScaleSetPriority.SPOT);
        return this;
    }

    /**
     * Specify that virtual machines should be spot priority VMs.
     *
     * @param policy eviction policy for the virtual machines.
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withSpotPriorityVirtualMachine(ScaleSetEvictionPolicy policy) {
        innerModel().withScaleSetPriority(ScaleSetPriority.SPOT);
        innerModel().withScaleSetEvictionPolicy(policy);
        return this;
    }

    /**
     * Sets the maximum price for virtual machine in agent pool. This price is in US Dollars.
     *
     * Default is -1 if not specified, as up to pay-as-you-go prices.
     *
     * @param maxPriceInUsDollars the maximum price in US Dollars
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withVirtualMachineMaximumPrice(Double maxPriceInUsDollars) {
        innerModel().withSpotMaxPrice(maxPriceInUsDollars.floatValue());
        return this;
    }

    /**
     * The OS disk type to be used for machines in the agent pool.
     *
     * The default is 'Ephemeral' if the VM supports it and has a cache disk larger than the requested
     * OSDiskSizeGB. Otherwise, defaults to 'Managed'.
     *
     * @param osDiskType the OS disk type to be used for machines in the agent pool
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withOSDiskType(OSDiskType osDiskType) {
        innerModel().withOsDiskType(osDiskType);
        return this;
    }

    /**
     * The disk type for the placement of emptyDir volumes, container runtime data root,
     * and Kubelet ephemeral storage.
     *
     * @param kubeletDiskType the disk type for the placement of emptyDir volumes, container runtime data root,
     *                        and Kubelet ephemeral storage.
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withKubeletDiskType(KubeletDiskType kubeletDiskType) {
        innerModel().withKubeletDiskType(kubeletDiskType);
        return this;
    }

    /**
     * Specifies tags for the agents.
     *
     * @param tags the tags to associate
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withTags(Map<String, String> tags) {
        innerModel().withTags(tags);
        return this;
    }

    /**
     * Adds a tag to the agents.
     * @param key the key for the tag
     * @param value the value for the tag
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withTag(String key, String value) {
        if (innerModel().tags() == null) {
            innerModel().withTags(new TreeMap<>());
        }
        innerModel().tags().put(key, value);
        return this;
    }

    /**
     * Specify to use an FIPS-enabled OS for agent pool machines.
     *
     * <p>See [Add a FIPS-enabled node
     * pool](https://docs.microsoft.com/azure/aks/use-multiple-node-pools#add-a-fips-enabled-node-pool-preview) for more
     * details.
     *
     * @return the AgentPoolData object itself
     */
    public AgentPoolData withFipsEnabled() {
        innerModel().withEnableFips(true);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentPoolInner innerModel() {
        return innerModel;
    }
}
