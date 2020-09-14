// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.implementation;

import com.azure.resourcemanager.containerservice.fluent.inner.AgentPoolInner;
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
        String subnetId = (inner != null) ? this.inner().vnetSubnetId() : null;
        this.subnetName = ResourceUtils.nameFromResourceId(subnetId);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public int count() {
        return this.inner().count();
    }

    @Override
    public ContainerServiceVMSizeTypes vmSize() {
        return this.inner().vmSize();
    }

    @Override
    public int osDiskSizeInGB() {
        return this.inner().osDiskSizeGB();
    }

    @Override
    public OSType osType() {
        return this.inner().osType();
    }

    @Override
    public AgentPoolType type() {
        return this.inner().type();
    }

    @Override
    public AgentPoolMode mode() {
        return this.inner().mode();
    }

    @Override
    public String subnetName() {
        if (this.subnetName != null) {
            return this.subnetName;
        } else {
            return ResourceUtils.nameFromResourceId(this.inner().vnetSubnetId());
        }
    }

    @Override
    public String networkId() {
        String subnetId = (this.inner() != null) ? this.inner().vnetSubnetId() : null;
        return (subnetId != null) ? ResourceUtils.parentResourceIdFromResourceId(subnetId) : null;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withVirtualMachineSize(ContainerServiceVMSizeTypes param0) {
        this.inner().withVmSize(param0);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withOSType(OSType osType) {
        this.inner().withOsType(osType);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withOSDiskSizeInGB(int osDiskSizeInGB) {
        this.inner().withOsDiskSizeGB(osDiskSizeInGB);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withAgentPoolType(AgentPoolType agentPoolType) {
        this.inner().withType(agentPoolType);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withAgentPoolTypeName(String agentPoolTypeName) {
        this.inner().withType(AgentPoolType.fromString(agentPoolTypeName));
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withAgentPoolVirtualMachineCount(int count) {
        this.inner().withCount(count);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withMaxPodsCount(int podsCount) {
        this.inner().withMaxPods(podsCount);
        return this;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withVirtualNetwork(String virtualNetworkId, String subnetName) {
        String vnetSubnetId = virtualNetworkId + "/subnets/" + subnetName;
        this.subnetName = subnetName;
        this.inner().withVnetSubnetId(vnetSubnetId);
        return this;
    }

    @Override
    public KubernetesClusterImpl attach() {
        return this.parent().addNewAgentPool(this);
    }

    public AgentPoolInner getAgentPoolInner() {
        AgentPoolInner agentPoolInner = new AgentPoolInner();
        agentPoolInner.withCount(inner().count());
        agentPoolInner.withVmSize(inner().vmSize());
        agentPoolInner.withOsDiskSizeGB(inner().osDiskSizeGB());
        agentPoolInner.withVnetSubnetId(inner().vnetSubnetId());
        agentPoolInner.withMaxPods(inner().maxPods());
        agentPoolInner.withOsType(inner().osType());
        agentPoolInner.withMaxCount(inner().maxCount());
        agentPoolInner.withMinCount(inner().minCount());
        agentPoolInner.withEnableAutoScaling(inner().enableAutoScaling());
        agentPoolInner.withTypePropertiesType(inner().type());
        agentPoolInner.withMode(inner().mode());
        agentPoolInner.withOrchestratorVersion(inner().orchestratorVersion());
        agentPoolInner.withNodeImageVersion(inner().nodeImageVersion());
        agentPoolInner.withUpgradeSettings(inner().upgradeSettings());
        agentPoolInner.withAvailabilityZones(inner().availabilityZones());
        agentPoolInner.withEnableNodePublicIp(inner().enableNodePublicIp());
        agentPoolInner.withScaleSetPriority(inner().scaleSetPriority());
        agentPoolInner.withScaleSetEvictionPolicy(inner().scaleSetEvictionPolicy());
        agentPoolInner.withSpotMaxPrice(inner().spotMaxPrice());
        agentPoolInner.withTags(inner().tags());
        agentPoolInner.withNodeLabels(inner().nodeLabels());
        agentPoolInner.withNodeTaints(inner().nodeTaints());
        agentPoolInner.withProximityPlacementGroupId(inner().proximityPlacementGroupId());
        return agentPoolInner;
    }

    @Override
    public KubernetesClusterAgentPoolImpl withAgentPoolMode(AgentPoolMode agentPoolMode) {
        inner().withMode(agentPoolMode);
        return this;
    }
}
