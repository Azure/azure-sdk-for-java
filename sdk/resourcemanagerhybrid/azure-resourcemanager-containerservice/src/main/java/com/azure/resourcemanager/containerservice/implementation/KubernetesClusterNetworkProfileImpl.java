// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.implementation;

import com.azure.resourcemanager.containerservice.models.ContainerServiceNetworkProfile;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.LoadBalancerSku;
import com.azure.resourcemanager.containerservice.models.NetworkPlugin;
import com.azure.resourcemanager.containerservice.models.NetworkPolicy;

/** The implementation for KubernetesClusterAgentPool and its create and update interfaces. */
public class KubernetesClusterNetworkProfileImpl
    implements KubernetesCluster.DefinitionStages.NetworkProfileDefinition<
        KubernetesCluster.DefinitionStages.WithCreate> {

    KubernetesClusterImpl parentKubernetesCluster;

    KubernetesClusterNetworkProfileImpl(KubernetesClusterImpl parent) {
        this.parentKubernetesCluster = parent;
    }

    @Override
    public KubernetesClusterNetworkProfileImpl withNetworkPlugin(NetworkPlugin networkPlugin) {
        ensureNetworkProfile().withNetworkPlugin(networkPlugin);
        return this;
    }

    @Override
    public KubernetesClusterNetworkProfileImpl withNetworkPolicy(NetworkPolicy networkPolicy) {
        ensureNetworkProfile().withNetworkPolicy(networkPolicy);
        return this;
    }

    @Override
    public KubernetesClusterNetworkProfileImpl withPodCidr(String podCidr) {
        ensureNetworkProfile().withPodCidr(podCidr);
        return this;
    }

    @Override
    public KubernetesClusterNetworkProfileImpl withServiceCidr(String serviceCidr) {
        ensureNetworkProfile().withServiceCidr(serviceCidr);
        return this;
    }

    @Override
    public KubernetesClusterNetworkProfileImpl withDnsServiceIP(String dnsServiceIP) {
        ensureNetworkProfile().withDnsServiceIp(dnsServiceIP);
        return this;
    }

    @Override
    public KubernetesClusterNetworkProfileImpl withDockerBridgeCidr(String dockerBridgeCidr) {
        ensureNetworkProfile().withDockerBridgeCidr(dockerBridgeCidr);
        return this;
    }

    @Override
    public KubernetesClusterNetworkProfileImpl withLoadBalancerSku(LoadBalancerSku loadBalancerSku) {
        ensureNetworkProfile().withLoadBalancerSku(loadBalancerSku);
        return this;
    }

    @Override
    public KubernetesClusterImpl attach() {
        return parentKubernetesCluster;
    }

    private ContainerServiceNetworkProfile ensureNetworkProfile() {
        if (this.parentKubernetesCluster.innerModel().networkProfile() == null) {
            this.parentKubernetesCluster.innerModel().withNetworkProfile(new ContainerServiceNetworkProfile());
        }
        return this.parentKubernetesCluster.innerModel().networkProfile();
    }
}
