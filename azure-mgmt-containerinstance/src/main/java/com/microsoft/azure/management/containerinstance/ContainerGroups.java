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
import rx.Observable;

/**
 * Entry point to the container instance management API.
 */
@Fluent()
@Beta(Beta.SinceVersion.V1_3_0)
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
     * @param containerName the container instance name
     * @param containerGroupName the container group name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return all available log lines
     */
    String getLogContent(String resourceGroupName, String containerName, String containerGroupName);

    /**
     * Get the log content for the specified container instance within a container group.
     *
     * @param resourceGroupName the Azure resource group name
     * @param containerName the container instance name
     * @param containerGroupName the container group name
     * @param tailLineCount only get the last log lines up to this
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the log lines from the end, up to the number specified
     */
    String getLogContent(String resourceGroupName, String containerName, String containerGroupName, int tailLineCount);

    /**
     * Get the log content for the specified container instance within a container group.
     *
     * @param resourceGroupName the Azure resource group name
     * @param containerName the container instance name
     * @param containerGroupName the container group name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Observable<String> getLogContentAsync(String resourceGroupName, String containerName, String containerGroupName);

    /**
     * Get the log content for the specified container instance within a container group.
     *
     * @param resourceGroupName the Azure resource group name
     * @param containerName the container instance name
     * @param containerGroupName the container group name
     * @param tailLineCount only get the last log lines up to this
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Observable<String> getLogContentAsync(String resourceGroupName, String containerName, String containerGroupName, int tailLineCount);
}
