// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.resourcemanager.containerservice.fluent.ManagedClustersClient;
import com.azure.resourcemanager.containerservice.fluent.models.CredentialResultsInner;
import com.azure.resourcemanager.containerservice.fluent.models.ManagedClusterInner;
import com.azure.resourcemanager.containerservice.fluent.models.OrchestratorVersionProfileListResultInner;
import com.azure.resourcemanager.containerservice.models.CredentialResult;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import com.azure.resourcemanager.containerservice.models.KubernetesClusters;
import com.azure.resourcemanager.containerservice.models.OrchestratorVersionProfile;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation for KubernetesClusters. */
public class KubernetesClustersImpl
    extends GroupableResourcesImpl<
        KubernetesCluster, KubernetesClusterImpl, ManagedClusterInner, ManagedClustersClient, ContainerServiceManager>
    implements KubernetesClusters {

    public KubernetesClustersImpl(final ContainerServiceManager containerServiceManager) {
        super(containerServiceManager.serviceClient().getManagedClusters(), containerServiceManager);
    }

    @Override
    public PagedIterable<KubernetesCluster> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedFlux<KubernetesCluster> listAsync() {
        return PagedConverter.mapPage(this.inner().listAsync(), inner -> new KubernetesClusterImpl(inner.name(), inner, manager()));
    }

    @Override
    public PagedIterable<KubernetesCluster> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(this.listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public PagedFlux<KubernetesCluster> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
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
        return new KubernetesClusterImpl(name, new ManagedClusterInner(), this.manager());
    }

    @Override
    protected KubernetesClusterImpl wrapModel(ManagedClusterInner inner) {
        if (inner == null) {
            return null;
        }

        return new KubernetesClusterImpl(inner.name(), inner, this.manager());
    }

    @Override
    public KubernetesClusterImpl define(String name) {
        return this.wrapModel(name);
    }

    @Override
    public Set<String> listKubernetesVersions(Region region) {
        TreeSet<String> kubernetesVersions = new TreeSet<>();
        OrchestratorVersionProfileListResultInner inner =
            this.manager().serviceClient().getContainerServices().listOrchestrators(region.name());

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
        return this
            .manager()
            .serviceClient()
            .getContainerServices()
            .listOrchestratorsAsync(region.name())
            .map(
                inner -> {
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
    public List<CredentialResult> listAdminKubeConfigContent(String resourceGroupName, String kubernetesClusterName) {
        return listAdminKubeConfigContentAsync(resourceGroupName, kubernetesClusterName).block();
    }

    @Override
    public Mono<List<CredentialResult>> listAdminKubeConfigContentAsync(
            String resourceGroupName, String kubernetesClusterName) {
        return this
            .manager()
            .serviceClient()
            .getManagedClusters()
            .listClusterAdminCredentialsAsync(resourceGroupName, kubernetesClusterName)
            .map(CredentialResultsInner::kubeconfigs);
    }

    @Override
    public List<CredentialResult> listUserKubeConfigContent(String resourceGroupName, String kubernetesClusterName) {
        return listUserKubeConfigContentAsync(resourceGroupName, kubernetesClusterName).block();
    }

    @Override
    public Mono<List<CredentialResult>> listUserKubeConfigContentAsync(
            String resourceGroupName, String kubernetesClusterName) {
        return this
            .manager()
            .serviceClient()
            .getManagedClusters()
            .listClusterUserCredentialsAsync(resourceGroupName, kubernetesClusterName)
            .map(CredentialResultsInner::kubeconfigs);
    }

    @Override
    public void start(String resourceGroupName, String kubernetesClusterName) {
        this.startAsync(resourceGroupName, kubernetesClusterName).block();
    }

    @Override
    public Mono<Void> startAsync(String resourceGroupName, String kubernetesClusterName) {
        return this.manager().serviceClient().getManagedClusters()
            .startAsync(resourceGroupName, kubernetesClusterName);
    }

    @Override
    public void stop(String resourceGroupName, String kubernetesClusterName) {
        this.stopAsync(resourceGroupName, kubernetesClusterName).block();
    }

    @Override
    public Mono<Void> stopAsync(String resourceGroupName, String kubernetesClusterName) {
        return this.manager().serviceClient().getManagedClusters()
            .stopAsync(resourceGroupName, kubernetesClusterName);
    }
}
