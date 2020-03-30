/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.compute.implementation.ComputeManager;
import com.azure.management.compute.models.SnapshotsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
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

/**
 * Entry point to managed snapshot management API in Azure.
 */
@Fluent
public interface Snapshots extends
        SupportsCreating<Snapshot.DefinitionStages.Blank>,
        SupportsListing<Snapshot>,
        SupportsListingByResourceGroup<Snapshot>,
        SupportsGettingByResourceGroup<Snapshot>,
        SupportsGettingById<Snapshot>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<Snapshot>,
        SupportsBatchDeletion,
        HasManager<ComputeManager>,
        HasInner<SnapshotsInner> {
    /**
     * Grants access to the snapshot asynchronously.
     *
     * @param resourceGroupName the resource group name
     * @param snapshotName the snapshot name
     * @param accessLevel access level
     * @param accessDuration access duration
     * @return a representation of the deferred computation of this call returning a read-only SAS URI to the snapshot
     */
    Mono<String> grantAccessAsync(String resourceGroupName,
                                  String snapshotName,
                                  AccessLevel accessLevel,
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
    String grantAccess(String resourceGroupName,
                       String snapshotName,
                       AccessLevel accessLevel,
                       int accessDuration);

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
