/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.compute.implementation.ComputeManager;
import com.azure.management.compute.models.DisksInner;
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
 * Entry point to managed disk management API in Azure.
 */
@Fluent
public interface Disks extends
        SupportsCreating<Disk.DefinitionStages.Blank>,
        SupportsListing<Disk>,
        SupportsListingByResourceGroup<Disk>,
        SupportsGettingByResourceGroup<Disk>,
        SupportsGettingById<Disk>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<Disk>,
        SupportsBatchDeletion,
        HasManager<ComputeManager>,
        HasInner<DisksInner> {

    /**
     * Grants access to a disk.
     *
     * @param resourceGroupName a resource group name
     * @param diskName a disk name
     * @param accessLevel access level
     * @param accessDuration access duration
     * @return the read-only SAS URI to the disk
     */
    String grantAccess(String resourceGroupName,
                       String diskName,
                       AccessLevel accessLevel,
                       int accessDuration);

    /**
     * Grants access to the disk asynchronously.
     *
     * @param resourceGroupName the resource group name
     * @param diskName the disk name
     * @param accessLevel access level
     * @param accessDuration access duration
     * @return a representation of the deferred computation of this call returning a read-only SAS URI to the disk
     */
    Mono<String> grantAccessAsync(String resourceGroupName,
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

    /**
     * Revoke access granted to the snapshot asynchronously.
     *
     * @param resourceGroupName the resource group name
     * @param diskName the disk name
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> revokeAccessAsync(String resourceGroupName, String diskName);
}
