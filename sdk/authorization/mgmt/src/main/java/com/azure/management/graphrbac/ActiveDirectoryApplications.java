// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.graphrbac;

import com.azure.core.annotation.Fluent;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.graphrbac.models.ApplicationsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/** Entry point to application management API. */
@Fluent
public interface ActiveDirectoryApplications
    extends SupportsListing<ActiveDirectoryApplication>,
        SupportsGettingById<ActiveDirectoryApplication>,
        SupportsGettingByName<ActiveDirectoryApplication>,
        SupportsCreating<ActiveDirectoryApplication.DefinitionStages.Blank>,
        SupportsBatchCreation<ActiveDirectoryApplication>,
        SupportsDeletingById,
        HasManager<GraphRbacManager>,
        HasInner<ApplicationsInner> {
}
