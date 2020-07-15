/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerinstance;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.containerinstance.implementation.ContainerGroupsInner;
import com.microsoft.azure.management.containerinstance.implementation.ContainerInstanceManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Completable;
import rx.Observable;

import java.util.List;
import java.util.Set;

/**
 * Entry point to the container instance management API.
 */
@Fluent()
@Beta(Beta.SinceVersion.V1_23_0)
public interface ContainerGroups extends
        SupportsCreating<ContainerGroup.DefinitionStages.Blank>,
        HasManager<ContainerInstanceManager>,
        HasInner<ContainerGroupsInner>,
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
    @Beta(Beta.SinceVersion.V1_5_0)
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
    @Beta(Beta.SinceVersion.V1_5_0)
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
    @Beta(Beta.SinceVersion.V1_5_0)
    Observable<String> getLogContentAsync(String resourceGroupName, String containerGroupName, String containerName);

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
    @Beta(Beta.SinceVersion.V1_5_0)
    Observable<String> getLogContentAsync(String resourceGroupName, String containerGroupName, String containerName, int tailLineCount);

    /**
     * Lists all operations for Azure Container Instance service.
     *
     * @return all operations for Azure Container Instance service
     */
    @Beta(Beta.SinceVersion.V1_5_0)
    Set<Operation> listOperations();

    /**
     * Lists all operations for Azure Container Instance service.
     *
     * @return a representation of the future computation of this call
     */
    @Beta(Beta.SinceVersion.V1_23_0)
    Observable<Set<Operation>> listOperationsAsync();

    /**
     * Lists cached images for a subscription in a region.
     *
     * @param location the identifier for the physical azure location.
     * @return all cached images from the specified location
     */
    @Beta(Beta.SinceVersion.V1_23_0)
    List<CachedImages> listCachedImages(String location);

    /**
     * Lists cached images for a subscription in a region.
     *
     * @param location the identifier for the physical azure location.
     * @return a representation of the future computation of this call
     */
    @Beta(Beta.SinceVersion.V1_23_0)
    Observable<CachedImages> listCachedImagesAsync(String location);

    /**
     * Lists the capabilities of a location.
     *
     * @param location the identifier for the physical azure location
     * @return a list of all of the capabilities of the given location
     */
    @Beta(Beta.SinceVersion.V1_23_0)
    List<Capabilities> listCapabilities(String location);

    /**
     * Lists the capabilities of a location.
     *
     * @param location the identifier for the physical azure location
     * @return a representation of the future computation of this call
     */
    @Beta(Beta.SinceVersion.V1_23_0)
    Observable<Capabilities> listCapabilitiesAsync(String location);

    /**
     * Starts all containers in a container group.
     *
     * @param resourceGroupName the name of the resource group of the container group
     * @param containerGroupName the name of the container group
     */
    @Beta(Beta.SinceVersion.V1_23_0)
    void start(String resourceGroupName, String containerGroupName);

    /**
     * Starts all containers in a container group.
     *
     * @param resourceGroupName the name of the resource group of the container group
     * @param containerGroupName the name of the container group
     * @return a representation of the future computation of this call
     */
    @Beta(Beta.SinceVersion.V1_23_0)
    Completable startAsync(String resourceGroupName, String containerGroupName);
}
