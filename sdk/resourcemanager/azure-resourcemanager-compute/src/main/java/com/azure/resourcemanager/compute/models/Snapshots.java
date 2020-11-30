// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
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
import reactor.core.publisher.Mono;

/** Entry point to managed snapshot management API in Azure. */
@Fluent
public interface Snapshots
    extends SupportsCreating<Snapshot.DefinitionStages.Blank>,
        SupportsListing<Snapshot>,
        SupportsListingByResourceGroup<Snapshot>,
        SupportsGettingByResourceGroup<Snapshot>,
        SupportsGettingById<Snapshot>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<Snapshot>,
        SupportsBatchDeletion,
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
    Mono<String> grantAccessAsync(
        String resourceGroupName, String snapshotName, AccessLevel accessLevel, int accessDuration);

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
}
