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
 * Entry point to managed disk management API in Azure.
 */
@Fluent
public interface Disks extends
        SupportsCreating<Disk.DefinitionStages.Blank>,
        SupportsListing<Disk>,
        SupportsListingByGroup<Disk>,
        SupportsGettingByGroup<Disk>,
        SupportsGettingById<Disk>,
        SupportsDeletingById,
        SupportsDeletingByGroup,
        SupportsBatchCreation<Disk> {
    /**
     * Grants access to a disk.
     *
     * @param resourceGroupName the resource group name
     * @param diskName the disk name
     * @param accessLevel access level
     * @param accessDuration access duration
     * @return the readonly SAS uri to the disk
     */
    String grantAccess(String resourceGroupName,
                       String diskName,
                       AccessLevel accessLevel,
                       int accessDuration);

    /**
     * Revoke access granted to a disk.
     *
     * @param resourceGroupName the resource group name
     * @param diskName the disk name
     */
    void revokeAccess(String resourceGroupName, String diskName);
}
