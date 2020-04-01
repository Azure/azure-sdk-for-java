/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac;

import com.azure.core.annotation.Fluent;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.graphrbac.models.GroupsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to AD group management API.
 */
@Fluent
public interface ActiveDirectoryGroups extends
        SupportsListing<ActiveDirectoryGroup>,
        SupportsGettingById<ActiveDirectoryGroup>,
        SupportsGettingByName<ActiveDirectoryGroup>,
        SupportsCreating<ActiveDirectoryGroup.DefinitionStages.Blank>,
        SupportsDeletingById,
        HasManager<GraphRbacManager>,
        HasInner<GroupsInner> {
}
