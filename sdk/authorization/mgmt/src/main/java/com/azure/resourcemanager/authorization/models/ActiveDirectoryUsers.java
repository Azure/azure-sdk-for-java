// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.UsersClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point to AD user management API. */
@Fluent
public interface ActiveDirectoryUsers
    extends SupportsGettingById<ActiveDirectoryUser>,
        SupportsGettingByName<ActiveDirectoryUser>,
        SupportsListing<ActiveDirectoryUser>,
        SupportsCreating<ActiveDirectoryUser.DefinitionStages.Blank>,
        SupportsDeletingById,
        HasManager<AuthorizationManager>,
        HasInner<UsersClient> {
}
