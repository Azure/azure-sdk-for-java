// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.resourcemanager.containerservice.fluent.ManagedClustersClient;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/** Entry point to managed Kubernetes service management API. */
@Fluent()
public interface KubernetesClusters
    extends HasManager<ContainerServiceManager>,
        HasInner<ManagedClustersClient>,
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
     * @param region the Azure region to query into
     * @return a set of Kubernetes versions which can be used when creating a service in this region
     */
    Set<String> listKubernetesVersions(Region region);

    /**
     * Returns the list of available Kubernetes versions available for the given Azure region.
     *
     * @param region the Azure region to query into
     * @return a future representation of a set of Kubernetes versions which can be used when creating a service in this
     *     region
     */
    Mono<Set<String>> listKubernetesVersionsAsync(Region region);

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
     * Returns asynchronously the user Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @return a future representation of the Kube.config content which can be used with a Kubernetes client
     */
    Mono<List<CredentialResult>> listUserKubeConfigContentAsync(String resourceGroupName, String kubernetesClusterName);
}
