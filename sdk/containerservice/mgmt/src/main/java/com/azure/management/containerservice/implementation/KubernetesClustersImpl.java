/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.containerservice.KubernetesCluster;
import com.azure.management.containerservice.KubernetesClusterAccessProfileRole;
import com.azure.management.containerservice.KubernetesClusters;
import com.azure.management.containerservice.OrchestratorVersionProfile;
import com.azure.management.containerservice.models.ManagedClusterAccessProfileInner;
import com.azure.management.containerservice.models.ManagedClusterInner;
import com.azure.management.containerservice.models.ManagedClustersInner;
import com.azure.management.containerservice.models.OrchestratorVersionProfileListResultInner;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * The implementation for KubernetesClusters.
 */
public class KubernetesClustersImpl extends
        GroupableResourcesImpl<
                            KubernetesCluster,
                        KubernetesClusterImpl,
                            ManagedClusterInner,
                            ManagedClustersInner,
                        ContainerServiceManager>
    implements KubernetesClusters {

    KubernetesClustersImpl(final ContainerServiceManager containerServiceManager) {
        super(containerServiceManager.inner().managedClusters(), containerServiceManager);
    }

    @Override
    public PagedIterable<KubernetesCluster> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedFlux<KubernetesCluster> listAsync() {
        return this.inner().listAsync().mapPage(inner -> new KubernetesClusterImpl(inner.getName(), inner, manager()));
    }

    @Override
    public PagedIterable<KubernetesCluster> listByResourceGroup(String resourceGroupName) {
        return wrapList(this.inner().listByResourceGroup(resourceGroupName));
    }

    @Override
    public PagedFlux<KubernetesCluster> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    protected Mono<ManagedClusterInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return this.inner().deleteAsync(resourceGroupName, name);
    }

    /**************************************************************
     * Fluent model helpers.
     **************************************************************/

    @Override
    protected KubernetesClusterImpl wrapModel(String name) {
        return new KubernetesClusterImpl(name,
            new ManagedClusterInner(),
            this.manager());
    }

    @Override
    protected KubernetesClusterImpl wrapModel(ManagedClusterInner inner) {
        if (inner == null) {
            return null;
        }

        return new KubernetesClusterImpl(inner.getName(),
            inner,
            this.manager());
    }

    @Override
    public KubernetesClusterImpl define(String name) {
        return this.wrapModel(name);
    }

    @Override
    public Set<String> listKubernetesVersions(Region region) {
        TreeSet<String> kubernetesVersions = new TreeSet<>();
        OrchestratorVersionProfileListResultInner inner = this.manager().inner().containerServices().listOrchestrators(region.name());

        if (inner != null && inner.orchestrators() != null && inner.orchestrators().size() > 0) {
            for (OrchestratorVersionProfile orchestrator : inner.orchestrators()) {
                if (orchestrator.orchestratorType().equals("Kubernetes")) {
                    kubernetesVersions.add(orchestrator.orchestratorVersion());
                }
            }
        }

        return Collections.unmodifiableSet(kubernetesVersions);
    }

    @Override
    public Mono<Set<String>> listKubernetesVersionsAsync(Region region) {
        return this.manager().inner().containerServices().listOrchestratorsAsync(region.name())
                .map(inner -> {
                    Set<String> kubernetesVersions = new TreeSet<>();
                    if (inner != null && inner.orchestrators() != null && inner.orchestrators().size() > 0) {
                        for (OrchestratorVersionProfile orchestrator : inner.orchestrators()) {
                            if (orchestrator.orchestratorType().equals("Kubernetes")) {
                                kubernetesVersions.add(orchestrator.orchestratorVersion());
                            }
                        }
                    }
                    return Collections.unmodifiableSet(kubernetesVersions);
                });
    }

    @Override
    public byte[] getAdminKubeConfigContent(String resourceGroupName, String kubernetesClusterName) {
        ManagedClusterAccessProfileInner profileInner = this.manager().inner().managedClusters().getAccessProfile(resourceGroupName, kubernetesClusterName, KubernetesClusterAccessProfileRole.ADMIN.toString());
        if (profileInner == null) {
            return new byte[0];
        } else {
            return profileInner.kubeConfig();
        }
    }

    @Override
    public Mono<byte[]> getAdminKubeConfigContentAsync(String resourceGroupName, String kubernetesClusterName) {
        return this.manager().inner().managedClusters()
            .getAccessProfileAsync(resourceGroupName, kubernetesClusterName, KubernetesClusterAccessProfileRole.ADMIN.toString())
            .map(profileInner -> {
                if (profileInner == null) {
                    return new byte[0];
                } else {
                    return profileInner.kubeConfig();
                }
            });
    }

    @Override
    public byte[] getUserKubeConfigContent(String resourceGroupName, String kubernetesClusterName) {
        ManagedClusterAccessProfileInner profileInner = this.manager().inner().managedClusters().getAccessProfile(resourceGroupName, kubernetesClusterName, KubernetesClusterAccessProfileRole.USER.toString());
        if (profileInner == null) {
            return new byte[0];
        } else {
            return profileInner.kubeConfig();
        }
    }

    @Override
    public Mono<byte[]> getUserKubeConfigContentAsync(String resourceGroupName, String kubernetesClusterName) {
        return this.manager().inner().managedClusters()
            .getAccessProfileAsync(resourceGroupName, kubernetesClusterName, KubernetesClusterAccessProfileRole.USER.toString())
            .map(profileInner -> {
                if (profileInner == null) {
                    return new byte[0];
                } else {
                    return profileInner.kubeConfig();
                }
            });
    }
}
