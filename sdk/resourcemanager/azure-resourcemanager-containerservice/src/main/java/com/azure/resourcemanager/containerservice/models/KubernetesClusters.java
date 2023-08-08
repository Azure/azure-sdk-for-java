// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/** Entry point to managed Kubernetes service management API. */
@Fluent
public interface KubernetesClusters
    extends HasManager<ContainerServiceManager>,
        SupportsCreating<KubernetesCluster.DefinitionStages.Blank>,
        SupportsBatchCreation<KubernetesCluster>,
        SupportsListing<KubernetesCluster>,
        SupportsGettingById<KubernetesCluster>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsListingByResourceGroup<KubernetesCluster>,
        SupportsGettingByResourceGroup<KubernetesCluster> {

    /**
     * Returns the list of available Kubernetes versions available for the given Azure region.
     *
     * @deprecated use {@link #listOrchestrators(Region, ContainerServiceResourceTypes)}
     * @param region the Azure region to query into
     * @return a set of Kubernetes versions which can be used when creating a service in this region
     */
    @Deprecated
    Set<String> listKubernetesVersions(Region region);

    /**
     * Returns the list of available Kubernetes versions available for the given Azure region.
     *
     * @deprecated use {@link #listOrchestratorsAsync(Region, ContainerServiceResourceTypes)}
     * @param region the Azure region to query into
     * @return a future representation of a set of Kubernetes versions which can be used when creating a service in this
     *     region
     */
    @Deprecated
    Mono<Set<String>> listKubernetesVersionsAsync(Region region);

    /**
     * Returns the list of available orchestrators for the given Azure region.
     *
     * @param region the Azure region to query into
     * @param resourceTypes the resource type of container service
     * @return a list of orchestrators which can be used when creating a service in this region
     */
    PagedIterable<OrchestratorVersionProfile> listOrchestrators(Region region,
                                                                ContainerServiceResourceTypes resourceTypes);

    /**
     * Returns the list of available orchestrators for the given Azure region.
     *
     * @param region the Azure region to query into
     * @param resourceTypes the resource type of container service
     * @return a list of orchestrators which can be used when creating a service in this region
     */
    PagedFlux<OrchestratorVersionProfile> listOrchestratorsAsync(Region region,
                                                                 ContainerServiceResourceTypes resourceTypes);

    /**
     * Returns the admin Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @return the Kube.config content which can be used with a Kubernetes client
     */
    List<CredentialResult> listAdminKubeConfigContent(String resourceGroupName, String kubernetesClusterName);

    /**
     * Returns asynchronously the admin Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @return a future representation of the Kube.config content which can be used with a Kubernetes client
     */
    Mono<List<CredentialResult>> listAdminKubeConfigContentAsync(
        String resourceGroupName, String kubernetesClusterName);

    /**
     * Returns the user Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @return the Kube.config content which can be used with a Kubernetes client
     */
    List<CredentialResult> listUserKubeConfigContent(String resourceGroupName, String kubernetesClusterName);

    /**
     * Returns the user Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @param format Only apply to AAD clusters, specifies the format of returned kubeconfig. Format 'azure' will return azure auth-provider kubeconfig; format 'exec' will return exec format kubeconfig, which requires kubelogin binary in the path.
     * @return the Kube.config content which can be used with a Kubernetes client
     */
    List<CredentialResult> listUserKubeConfigContent(String resourceGroupName, String kubernetesClusterName, Format format);

    /**
     * Returns asynchronously the user Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @return a future representation of the Kube.config content which can be used with a Kubernetes client
     */
    Mono<List<CredentialResult>> listUserKubeConfigContentAsync(String resourceGroupName, String kubernetesClusterName);

    /**
     * Returns asynchronously the user Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @param format Only apply to AAD clusters, specifies the format of returned kubeconfig. Format 'azure' will return azure auth-provider kubeconfig; format 'exec' will return exec format kubeconfig, which requires kubelogin binary in the path.
     * @return a future representation of the Kube.config content which can be used with a Kubernetes client
     */
    Mono<List<CredentialResult>> listUserKubeConfigContentAsync(String resourceGroupName, String kubernetesClusterName, Format format);

    /**
     * Starts a stopped Kubernetes cluster.
     *
     * @param resourceGroupName The name of the resource group.
     * @param kubernetesClusterName The name of the managed cluster resource.
     */
    void start(String resourceGroupName, String kubernetesClusterName);

    /**
     * Starts a stopped Kubernetes cluster.
     *
     * @param resourceGroupName The name of the resource group.
     * @param kubernetesClusterName The name of the managed cluster resource.
     * @return the completion.
     */
    Mono<Void> startAsync(String resourceGroupName, String kubernetesClusterName);

    /**
     * Stops a running Kubernetes cluster.
     *
     * @param resourceGroupName The name of the resource group.
     * @param kubernetesClusterName The name of the managed cluster resource.
     */
    void stop(String resourceGroupName, String kubernetesClusterName);

    /**
     * Stops a running Kubernetes cluster.
     *
     * @param resourceGroupName The name of the resource group.
     * @param kubernetesClusterName The name of the managed cluster resource.
     * @return the completion.
     */
    Mono<Void> stopAsync(String resourceGroupName, String kubernetesClusterName);
}
