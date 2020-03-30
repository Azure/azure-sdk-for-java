/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerservice;

import com.azure.core.annotation.Fluent;
import com.azure.management.containerservice.implementation.ContainerServiceManager;
import com.azure.management.containerservice.models.ManagedClustersInner;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 *  Entry point to managed Kubernetes service management API.
 */
@Fluent()
public interface KubernetesClusters extends
        HasManager<ContainerServiceManager>,
        HasInner<ManagedClustersInner>,
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
     * @return a future representation of a set of Kubernetes versions which can be used when creating a service in this region
     */
    Mono<Set<String>> listKubernetesVersionsAsync(Region region);

    /**
     * Returns the admin Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @return the Kube.config content which can be used with a Kubernetes client
     */
    byte[] getAdminKubeConfigContent(String resourceGroupName, String kubernetesClusterName);

    /**
     * Returns asynchronously the admin Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @return a future representation of the Kube.config content which can be used with a Kubernetes client
     */
    Mono<byte[]> getAdminKubeConfigContentAsync(String resourceGroupName, String kubernetesClusterName);

    /**
     * Returns the user Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @return the Kube.config content which can be used with a Kubernetes client
     */
    byte[] getUserKubeConfigContent(String resourceGroupName, String kubernetesClusterName);

    /**
     * Returns asynchronously the user Kube.config content which can be used with a Kubernetes client.
     *
     * @param resourceGroupName the resource group name where the cluster is
     * @param kubernetesClusterName the managed cluster name
     * @return a future representation of the Kube.config content which can be used with a Kubernetes client
     */
    Mono<byte[]> getUserKubeConfigContentAsync(String resourceGroupName, String kubernetesClusterName);
}
