// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.implementation;

import com.azure.resourcemanager.containerservice.fluent.models.AgentPoolInner;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.AgentPoolType;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool;
import com.azure.resourcemanager.containerservice.models.ManagedClusterAgentPoolProfile;
import com.azure.resourcemanager.containerservice.models.OSType;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/** The implementation for KubernetesClusterAgentPool and its create and update interfaces. */
public class KubernetesClusterAgentPoolImpl
    extends ChildResourceImpl<ManagedClusterAgentPoolProfile, KubernetesClusterImpl, KubernetesCluster>
    implements KubernetesClusterAgentPool,
    KubernetesClusterAgentPool.Definition<KubernetesClusterImpl>,
    KubernetesClusterAgentPool.Update<KubernetesClusterImpl> {

    private String subnetName;

    KubernetesClusterAgentPoolImpl(ManagedClusterAgentPoolProfile inner, KubernetesClusterImpl parent) {
        super(inner, parent);
        String subnetId = (inner != null) ? this.innerModel().vnetSubnetId() : null;
        this.subnetName = ResourceUtils.nameFromResourceId(subnetId);
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public int count() {
        return this.innerModel().count();
    }

    @Override
    public ContainerServiceVMSizeTypes vmSize() {
        return this.innerModel().vmSize();
    }

    @Override
    public int osDiskSizeInGB() {
        return this.innerModel().osDiskSizeGB();
    }

    @Override
    public OSType osType() {
        return this.innerModel().osType();
    }

    @Override
    public AgentPoolType type() {
        return this.innerModel().type();
    }

    @Override
    public AgentPoolMode mode() {
        return this.innerModel().mode();
    }

    @Override
    public String subnetName() {
        if (this.subnetName != null) {
            return this.subnetName;
        } else {
            return ResourceUtils.nameFromResourceId(this.innerModel().vnetSubnetId());
        }
    }

    @Override
    public String networkId() {
        String subnetId = (this.innerModel() != null) ? this.innerModel().vnetSubnetId() : null;
        return (subnetId != null) ? ResourceUtils.parentResourceIdFromResourceId(subnetId) : null;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withVirtualMachineSize(ContainerServiceVMSizeTypes param0) {
        this.innerModel().withVmSize(param0);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withOSType(OSType osType) {
        this.innerModel().withOsType(osType);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withOSDiskSizeInGB(int osDiskSizeInGB) {
        this.innerModel().withOsDiskSizeGB(osDiskSizeInGB);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withAgentPoolType(AgentPoolType agentPoolType) {
        this.innerModel().withType(agentPoolType);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withAgentPoolTypeName(String agentPoolTypeName) {
        this.innerModel().withType(AgentPoolType.fromString(agentPoolTypeName));
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withAgentPoolVirtualMachineCount(int count) {
        this.innerModel().withCount(count);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withMaxPodsCount(int podsCount) {
        this.innerModel().withMaxPods(podsCount);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withVirtualNetwork(String virtualNetworkId, String subnetName) {
        String vnetSubnetId = virtualNetworkId + "/subnets/" + subnetName;
        this.subnetName = subnetName;
        this.innerModel().withVnetSubnetId(vnetSubnetId);
        return this;
    }

    @Override
    public KubernetesClusterImpl attach() {
        return this.parent().addNewAgentPool(this);
    }

    public AgentPoolInner getAgentPoolInner() {
        AgentPoolInner agentPoolInner = new AgentPoolInner();
        agentPoolInner.withCount(innerModel().count());
        agentPoolInner.withVmSize(innerModel().vmSize());
        agentPoolInner.withOsDiskSizeGB(innerModel().osDiskSizeGB());
        agentPoolInner.withVnetSubnetId(innerModel().vnetSubnetId());
        agentPoolInner.withMaxPods(innerModel().maxPods());
        agentPoolInner.withOsType(innerModel().osType());
        agentPoolInner.withMaxCount(innerModel().maxCount());
        agentPoolInner.withMinCount(innerModel().minCount());
        agentPoolInner.withEnableAutoScaling(innerModel().enableAutoScaling());
        agentPoolInner.withTypePropertiesType(innerModel().type());
        agentPoolInner.withMode(innerModel().mode());
        agentPoolInner.withOrchestratorVersion(innerModel().orchestratorVersion());
        agentPoolInner.withNodeImageVersion(innerModel().nodeImageVersion());
        agentPoolInner.withUpgradeSettings(innerModel().upgradeSettings());
        agentPoolInner.withAvailabilityZones(innerModel().availabilityZones());
        agentPoolInner.withEnableNodePublicIp(innerModel().enableNodePublicIp());
        agentPoolInner.withScaleSetPriority(innerModel().scaleSetPriority());
        agentPoolInner.withScaleSetEvictionPolicy(innerModel().scaleSetEvictionPolicy());
        agentPoolInner.withSpotMaxPrice(innerModel().spotMaxPrice());
        agentPoolInner.withTags(innerModel().tags());
        agentPoolInner.withNodeLabels(innerModel().nodeLabels());
        agentPoolInner.withNodeTaints(innerModel().nodeTaints());
        agentPoolInner.withProximityPlacementGroupId(innerModel().proximityPlacementGroupId());
        return agentPoolInner;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withAgentPoolMode(AgentPoolMode agentPoolMode) {
        innerModel().withMode(agentPoolMode);
        return this;
    }
}
