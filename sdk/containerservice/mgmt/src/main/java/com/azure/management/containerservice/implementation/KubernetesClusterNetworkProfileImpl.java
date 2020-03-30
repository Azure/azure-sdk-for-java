/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerservice.implementation;

import com.azure.management.containerservice.ContainerServiceNetworkProfile;
import com.azure.management.containerservice.KubernetesCluster;
import com.azure.management.containerservice.NetworkPlugin;
import com.azure.management.containerservice.NetworkPolicy;

/**
 * The implementation for KubernetesClusterAgentPool and its create and update interfaces.
 */
public class KubernetesClusterNetworkProfileImpl
    implements KubernetesCluster.DefinitionStages.NetworkProfileDefinition<KubernetesCluster.DefinitionStages.WithCreate> {

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
        ensureNetworkProfile().withDnsServiceIP(dnsServiceIP);
        return this;
    }

    @Override
    public KubernetesClusterNetworkProfileImpl withDockerBridgeCidr(String dockerBridgeCidr) {
        ensureNetworkProfile().withDockerBridgeCidr(dockerBridgeCidr);
        return this;
    }

    @Override
    public KubernetesClusterImpl attach() {
        return parentKubernetesCluster;
    }

    private ContainerServiceNetworkProfile ensureNetworkProfile() {
        if (this.parentKubernetesCluster.inner().networkProfile() == null) {
            this.parentKubernetesCluster.inner().withNetworkProfile(new ContainerServiceNetworkProfile());
        }
        return this.parentKubernetesCluster.inner().networkProfile();
    }

}
