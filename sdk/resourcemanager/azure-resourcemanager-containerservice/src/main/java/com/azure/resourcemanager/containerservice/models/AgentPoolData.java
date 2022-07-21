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
        this.innerModel = new AgentPoolInner();
    }

    /**
     * Creates an instance of agent pool data.
     *
     * @param innerModel the inner model of agent pool.
     */
    public AgentPoolData(AgentPoolInner innerModel) {
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
    public Map<String, String> tags() {
        return innerModel().tags();
    }

    public AgentPoolData withVirtualMachineSize(ContainerServiceVMSizeTypes vmSize) {
        this.innerModel().withVmSize(vmSize.toString());
        return this;
    }

    public AgentPoolData withOSType(OSType osType) {
        this.innerModel().withOsType(osType);
        return this;
    }

    public AgentPoolData withOSDiskSizeInGB(int osDiskSizeInGB) {
        this.innerModel().withOsDiskSizeGB(osDiskSizeInGB);
        return this;
    }

    public AgentPoolData withAgentPoolType(AgentPoolType agentPoolType) {
        this.innerModel().withTypePropertiesType(agentPoolType);
        return this;
    }

    public AgentPoolData withAgentPoolTypeName(String agentPoolTypeName) {
        this.innerModel().withTypePropertiesType(AgentPoolType.fromString(agentPoolTypeName));
        return this;
    }

    public AgentPoolData withAgentPoolVirtualMachineCount(int count) {
        this.innerModel().withCount(count);
        return this;
    }

    public AgentPoolData withMaxPodsCount(int podsCount) {
        this.innerModel().withMaxPods(podsCount);
        return this;
    }

    public AgentPoolData withVirtualNetwork(String virtualNetworkId, String subnetName) {
        String vnetSubnetId = virtualNetworkId + "/subnets/" + subnetName;
        this.innerModel().withVnetSubnetId(vnetSubnetId);
        return this;
    }

    public AgentPoolData withAgentPoolMode(AgentPoolMode agentPoolMode) {
        innerModel().withMode(agentPoolMode);
        return this;
    }

    public AgentPoolData withAutoScaling(int minimumNodeSize, int maximumNodeSize) {
        innerModel().withEnableAutoScaling(true);
        innerModel().withMinCount(minimumNodeSize);
        innerModel().withMaxCount(maximumNodeSize);
        return this;
    }

    public AgentPoolData withAvailabilityZones(Integer... zones) {
        innerModel().withAvailabilityZones(Arrays.stream(zones).map(String::valueOf).collect(Collectors.toList()));
        return this;
    }

    public AgentPoolData withNodeLabels(Map<String, String> nodeLabels) {
        innerModel().withNodeLabels(nodeLabels == null ? null : new TreeMap<>(nodeLabels));
        return this;
    }

    public AgentPoolData withNodeTaints(List<String> nodeTaints) {
        innerModel().withNodeTaints(nodeTaints);
        return this;
    }

    public AgentPoolData withVirtualMachinePriority(ScaleSetPriority priority) {
        innerModel().withScaleSetPriority(priority);
        return this;
    }

    public AgentPoolData withSpotPriorityVirtualMachine() {
        innerModel().withScaleSetPriority(ScaleSetPriority.SPOT);
        return this;
    }

    public AgentPoolData withSpotPriorityVirtualMachine(ScaleSetEvictionPolicy policy) {
        innerModel().withScaleSetPriority(ScaleSetPriority.SPOT);
        innerModel().withScaleSetEvictionPolicy(policy);
        return this;
    }

    public AgentPoolData withVirtualMachineMaximumPrice(Double maxPriceInUsDollars) {
        innerModel().withSpotMaxPrice(maxPriceInUsDollars.floatValue());
        return this;
    }

    public AgentPoolData withOSDiskType(OSDiskType osDiskType) {
        innerModel().withOsDiskType(osDiskType);
        return this;
    }

    public AgentPoolData withKubeletDiskType(KubeletDiskType kubeletDiskType) {
        innerModel().withKubeletDiskType(kubeletDiskType);
        return this;
    }

    public AgentPoolData withTags(Map<String, String> tags) {
        innerModel().withTags(tags);
        return this;
    }

    public AgentPoolData withTag(String key, String value) {
        if (innerModel().tags() == null) {
            innerModel().withTags(new TreeMap<>());
        }
        innerModel().tags().put(key, value);
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
