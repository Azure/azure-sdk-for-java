// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.implementation;

import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.AgentPoolType;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool;
import com.azure.resourcemanager.containerservice.models.ManagedClusterAgentPoolProfile;
import com.azure.resourcemanager.containerservice.models.OSType;
import com.azure.resourcemanager.containerservice.models.OrchestratorServiceBase;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/** The implementation for KubernetesClusterAgentPool and its create and update interfaces. */
public class KubernetesClusterAgentPoolImpl
    extends ChildResourceImpl<ManagedClusterAgentPoolProfile, KubernetesClusterImpl, OrchestratorServiceBase>
    implements KubernetesClusterAgentPool,
    KubernetesClusterAgentPool.Definition<KubernetesCluster.DefinitionStages.WithCreate> {

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
        if (inner().mode() == null) {
            inner().withMode(AgentPoolMode.SYSTEM);
        }
        this.parent().inner().agentPoolProfiles().add(this.inner());
        return this.parent();
    }

    @Override
    public KubernetesClusterAgentPoolImpl withAgentPoolMode(AgentPoolMode agentPoolMode) {
        inner().withMode(agentPoolMode);
        return this;
    }
}
