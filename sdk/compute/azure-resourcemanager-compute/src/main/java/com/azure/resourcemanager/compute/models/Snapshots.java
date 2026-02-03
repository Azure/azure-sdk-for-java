// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;
import com.azure.resourcemanager.compute.ComputeManager;
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
import reactor.core.publisher.Mono;

/** Entry point to managed snapshot management API in Azure. */
@Fluent
public interface Snapshots extends SupportsCreating<Snapshot.DefinitionStages.Blank>, SupportsListing<Snapshot>,
    SupportsListingByResourceGroup<Snapshot>, SupportsGettingByResourceGroup<Snapshot>, SupportsGettingById<Snapshot>,
    SupportsDeletingById, SupportsDeletingByResourceGroup, SupportsBatchCreation<Snapshot>, SupportsBatchDeletion,
    HasManager<ComputeManager> {
    /**
     * Grants access to the snapshot asynchronously.
     *
     * @param resourceGroupName the resource group name
     * @param snapshotName the snapshot name
     * @param accessLevel access level
     * @param accessDuration access duration
     * @return a representation of the deferred computation of this call returning a read-only SAS URI to the snapshot
     */
    Mono<String> grantAccessAsync(String resourceGroupName, String snapshotName, AccessLevel accessLevel,
        int accessDuration);

    /**
     * Grants access to a snapshot.
     *
     * @param resourceGroupName the resource group name
     * @param snapshotName the snapshot name
     * @param accessLevel access level
     * @param accessDuration access duration
     * @return the read-only SAS URI to the snapshot
     */
    String grantAccess(String resourceGroupName, String snapshotName, AccessLevel accessLevel, int accessDuration);

    /**
     * Revoke access granted to the snapshot asynchronously.
     *
     * @param resourceGroupName the resource group name
     * @param snapName the snapshot name
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> revokeAccessAsync(String resourceGroupName, String snapName);

    /**
     * Revoke access granted to a snapshot.
     *
     * @param resourceGroupName the resource group name
     * @param snapName the snapshot name
     */
    void revokeAccess(String resourceGroupName, String snapName);

    /**
     * Begins deleting a snapshot from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the snapshot to delete
     * @return the accepted deleting operation
     */
    default Accepted<Void> beginDeleteById(String id) {
        throw new UnsupportedOperationException("[beginDeleteById(String)] is not supported in " + getClass());
    }

    /**
     * Begins deleting a snapshot from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the snapshot to delete
     * @param context the {@link Context} of the request
     * @return the accepted deleting operation
     */
    default Accepted<Void> beginDeleteById(String id, Context context) {
        throw new UnsupportedOperationException("[beginDeleteById(String, Context)] is not supported in " + getClass());
    }

    /**
     * Begins deleting a snapshot from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param name the snapshot name
     * @return the accepted deleting operation
     */
    default Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name) {
        throw new UnsupportedOperationException(
            "[beginDeleteByResourceGroup(String, String)] is not supported in " + getClass());
    }

    /**
     * Begins deleting a snapshot from Azure, identifying it by its name and its resource group.
     *
     * @param resourceGroupName the resource group the resource is part of
     * @param name the snapshot name
     * @param context the {@link Context} of the request
     * @return the accepted deleting operation
     */
    default Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name, Context context) {
        throw new UnsupportedOperationException(
            "[beginDeleteByResource(String, String, Context)] is not supported in " + getClass());
    }
}
