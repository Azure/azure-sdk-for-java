// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.DisksClient;
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
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/** Entry point to managed disk management API in Azure. */
@Fluent
public interface Disks
    extends SupportsCreating<Disk.DefinitionStages.Blank>,
        SupportsListing<Disk>,
        SupportsListingByResourceGroup<Disk>,
        SupportsGettingByResourceGroup<Disk>,
        SupportsGettingById<Disk>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<Disk>,
        SupportsBatchDeletion,
        HasManager<ComputeManager>,
        HasInner<DisksClient> {

    /**
     * Grants access to a disk.
     *
     * @param resourceGroupName a resource group name
     * @param diskName a disk name
     * @param accessLevel access level
     * @param accessDuration access duration
     * @return the read-only SAS URI to the disk
     */
    String grantAccess(String resourceGroupName, String diskName, AccessLevel accessLevel, int accessDuration);

    /**
     * Grants access to the disk asynchronously.
     *
     * @param resourceGroupName the resource group name
     * @param diskName the disk name
     * @param accessLevel access level
     * @param accessDuration access duration
     * @return a representation of the deferred computation of this call returning a read-only SAS URI to the disk
     */
    Mono<String> grantAccessAsync(
        String resourceGroupName, String diskName, AccessLevel accessLevel, int accessDuration);

    /**
     * Revoke access granted to a disk.
     *
     * @param resourceGroupName the resource group name
     * @param diskName the disk name
     */
    void revokeAccess(String resourceGroupName, String diskName);

    /**
     * Revoke access granted to the snapshot asynchronously.
     *
     * @param resourceGroupName the resource group name
     * @param diskName the disk name
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> revokeAccessAsync(String resourceGroupName, String diskName);

    /**
     * Begins deleting a disk from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the disk to delete
     * @return the accepted deleting operation
     */
    Accepted<Void> beginDeleteById(String id);

    /**
     * Begins deleting a disk from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param name the disk name
     * @return the accepted deleting operation
     */
    Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name);
}
