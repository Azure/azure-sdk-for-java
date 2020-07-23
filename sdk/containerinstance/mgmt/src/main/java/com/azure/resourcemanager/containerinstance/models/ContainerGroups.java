// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerinstance.ContainerInstanceManager;
import com.azure.resourcemanager.containerinstance.fluent.ContainerGroupsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
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

/** Entry point to the container instance management API. */
@Fluent
public interface ContainerGroups
    extends SupportsCreating<ContainerGroup.DefinitionStages.Blank>,
        HasManager<ContainerInstanceManager>,
        HasInner<ContainerGroupsClient>,
        SupportsBatchCreation<ContainerGroup>,
        SupportsGettingByResourceGroup<ContainerGroup>,
        SupportsGettingById<ContainerGroup>,
        SupportsDeletingByResourceGroup,
        SupportsDeletingById,
        SupportsBatchDeletion,
        SupportsListingByResourceGroup<ContainerGroup>,
        SupportsListing<ContainerGroup> {

    /**
     * Get the log content for the specified container instance within a container group.
     *
     * @param resourceGroupName the Azure resource group name
     * @param containerGroupName the container group name
     * @param containerName the container instance name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return all available log lines
     */
    String getLogContent(String resourceGroupName, String containerGroupName, String containerName);

    /**
     * Get the log content for the specified container instance within a container group.
     *
     * @param resourceGroupName the Azure resource group name
     * @param containerGroupName the container group name
     * @param containerName the container instance name
     * @param tailLineCount only get the last log lines up to this
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the log lines from the end, up to the number specified
     */
    String getLogContent(String resourceGroupName, String containerGroupName, String containerName, int tailLineCount);

    /**
     * Get the log content for the specified container instance within a container group.
     *
     * @param resourceGroupName the Azure resource group name
     * @param containerGroupName the container group name
     * @param containerName the container instance name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Mono<String> getLogContentAsync(String resourceGroupName, String containerGroupName, String containerName);

    /**
     * Get the log content for the specified container instance within a container group.
     *
     * @param resourceGroupName the Azure resource group name
     * @param containerGroupName the container group name
     * @param containerName the container instance name
     * @param tailLineCount only get the last log lines up to this
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Mono<String> getLogContentAsync(
        String resourceGroupName, String containerGroupName, String containerName, int tailLineCount);

    /**
     * Lists all operations for Azure Container Instance service.
     *
     * @return all operations for Azure Container Instance service
     */
    PagedIterable<Operation> listOperations();

    /**
     * Lists all operations for Azure Container Instance service.
     *
     * @return a representation of the future computation of this call
     */
    PagedFlux<Operation> listOperationsAsync();

    /**
     * Lists cached images for a subscription in a region.
     *
     * @param location the identifier for the physical azure location.
     * @return all cached images from the specified location
     */
    PagedIterable<CachedImages> listCachedImages(String location);

    /**
     * Lists cached images for a subscription in a region.
     *
     * @param location the identifier for the physical azure location.
     * @return a representation of the future computation of this call
     */
    PagedFlux<CachedImages> listCachedImagesAsync(String location);

    /**
     * Lists the capabilities of a location.
     *
     * @param location the identifier for the physical azure location
     * @return a list of all of the capabilities of the given location
     */
    PagedIterable<Capabilities> listCapabilities(String location);

    /**
     * Lists the capabilities of a location.
     *
     * @param location the identifier for the physical azure location
     * @return a representation of the future computation of this call
     */
    PagedFlux<Capabilities> listCapabilitiesAsync(String location);

    /**
     * Starts all containers in a container group.
     *
     * @param resourceGroupName the name of the resource group of the container group
     * @param containerGroupName the name of the container group
     */
    void start(String resourceGroupName, String containerGroupName);

    /**
     * Starts all containers in a container group.
     *
     * @param resourceGroupName the name of the resource group of the container group
     * @param containerGroupName the name of the container group
     * @return a representation of the future computation of this call
     */
    Mono<Void> startAsync(String resourceGroupName, String containerGroupName);
}
