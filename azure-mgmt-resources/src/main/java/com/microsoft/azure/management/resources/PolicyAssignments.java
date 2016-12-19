/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to policy assignment management API.
 */
@Fluent
public interface PolicyAssignments extends
        SupportsListing<PolicyAssignment>,
        SupportsListingByGroup<PolicyAssignment>,
        SupportsGettingById<PolicyAssignment>,
        SupportsCreating<PolicyAssignment.DefinitionStages.Blank>,
        SupportsDeletingById {
    /**
     * List policy assignments of the resource.
     *
     * @param resourceId the ID of the resource
     * @return the list of policy assignments
     */
    PagedList<PolicyAssignment> listByResource(final String resourceId);
}
