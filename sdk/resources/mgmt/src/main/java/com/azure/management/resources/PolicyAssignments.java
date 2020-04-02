/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;

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
    PagedIterable<PolicyAssignment> listByResource(final String resourceId);
}
