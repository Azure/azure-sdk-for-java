// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingByFilter;

/** Entry point to AD group management API. */
@Fluent
public interface ActiveDirectoryGroups
    extends SupportsListing<ActiveDirectoryGroup>,
        SupportsListingByFilter<ActiveDirectoryGroup>,
        SupportsGettingById<ActiveDirectoryGroup>,
        SupportsGettingByName<ActiveDirectoryGroup>,
        SupportsCreating<ActiveDirectoryGroup.DefinitionStages.Blank>,
        SupportsDeletingById,
        HasManager<AuthorizationManager> {
}
