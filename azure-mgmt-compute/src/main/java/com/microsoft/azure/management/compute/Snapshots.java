/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to managed snapshot management API in Azure.
 */
@Fluent
public interface Snapshots extends
        SupportsCreating<Snapshot.DefinitionStages.Blank>,
        SupportsListing<Snapshot>,
        SupportsListingByGroup<Snapshot>,
        SupportsGettingByGroup<Snapshot>,
        SupportsGettingById<Snapshot>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsBatchCreation<Snapshot> {
    /**
     * Grants access to a snapshot.
     *
     * @param resourceGroupName the resource group name
     * @param snapshotName the snapshot name
     * @param accessLevel access level
     * @param accessDuration access duration
     * @return the readonly SAS uri to the snapshot
     */
    String grantAccess(String resourceGroupName,
                       String snapshotName,
                       AccessLevel accessLevel,
                       int accessDuration);

    /**
     * Revoke access granted to a snapshot.
     *
     * @param resourceGroupName the resource group name
     * @param snapName the snapshot name
     */
    void revokeAccess(String resourceGroupName, String snapName);
}
