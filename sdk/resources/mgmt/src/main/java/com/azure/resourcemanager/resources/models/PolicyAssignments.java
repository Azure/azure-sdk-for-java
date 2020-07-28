// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to policy assignment management API.
 */
@Fluent
public interface PolicyAssignments extends
        SupportsListing<PolicyAssignment>,
        SupportsListingByResourceGroup<PolicyAssignment>,
        SupportsGettingById<PolicyAssignment>,
        SupportsCreating<PolicyAssignment.DefinitionStages.Blank>,
        SupportsDeletingById {
    /**
     * List policy assignments of the resource.
     *
     * @param resourceId the ID of the resource
     * @return the list of policy assignments
     */
    PagedIterable<PolicyAssignment> listByResource(String resourceId);
}
