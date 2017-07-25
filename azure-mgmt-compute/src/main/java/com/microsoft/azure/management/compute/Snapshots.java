/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.SnapshotsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

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
    @Beta(Beta.SinceVersion.V1_2_0)
    Observable<String> grantAccessAsync(String resourceGroupName,
                                        String snapshotName,
                                        AccessLevel accessLevel,
                                        int accessDuration);

    /**
     * Grants access to the snapshot asynchronously.
     *
     * @param resourceGroupName the resource group name
     * @param snapshotName the snapshot name
     * @param accessLevel access level
     * @param accessDuration access duration
     * @param callback the callback to call on success or failure, on success it will pass read-only SAS URI to the snapshot in callback
     * @return a handle to cancel the request
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    ServiceFuture<String> grantAccessAsync(String resourceGroupName,
                                           String snapshotName,
                                           AccessLevel accessLevel,
                                           int accessDuration,
                                           ServiceCallback<String> callback);

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
    @Beta(Beta.SinceVersion.V1_2_0)
    Completable revokeAccessAsync(String resourceGroupName, String snapName);

    /**
     * Revoke access granted to the snapshot asynchronously.
     *
     * @param resourceGroupName the resource group name
     * @param snapName the snapshot name
     * @param callback the callback to call on success or failure
     * @return a handle to cancel the request
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    ServiceFuture<Void> revokeAccessAsync(String resourceGroupName, String snapName, ServiceCallback<Void> callback);

    /**
     * Revoke access granted to a snapshot.
     *
     * @param resourceGroupName the resource group name
     * @param snapName the snapshot name
     */
    void revokeAccess(String resourceGroupName, String snapName);
}
