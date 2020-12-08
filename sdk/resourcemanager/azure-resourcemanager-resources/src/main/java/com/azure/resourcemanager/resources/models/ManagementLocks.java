// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.ResourceManager;
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

/**
 * Entry point to management lock management.
 */
@Fluent
public interface ManagementLocks extends
    SupportsListing<ManagementLock>,
    SupportsCreating<ManagementLock.DefinitionStages.Blank>,
    SupportsDeletingById,
    SupportsListingByResourceGroup<ManagementLock>,
    SupportsGettingByResourceGroup<ManagementLock>,
    SupportsGettingById<ManagementLock>,
    SupportsDeletingByResourceGroup,
    SupportsBatchCreation<ManagementLock>,
    SupportsBatchDeletion,
    HasManager<ResourceManager> {

    /**
     * Lists management locks associated with the specified resource, its resource group
     * and any resources below the resource.
     *
     * @param resourceId the resource ID of the resource
     * @return management locks
     */
    PagedIterable<ManagementLock> listForResource(String resourceId);

    /**
     * Lists management locks associated with the specified resource, its resource group, and
     * any level below the resource.
     *
     * @param resourceId the resource Id of the resource
     * @return management locks
     */
    PagedFlux<ManagementLock> listForResourceAsync(String resourceId);
}
