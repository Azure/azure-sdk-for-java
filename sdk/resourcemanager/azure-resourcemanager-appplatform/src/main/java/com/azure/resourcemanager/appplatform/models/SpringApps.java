// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.implementation.annotation.Beta;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.AppsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point for Spring Apps API. */
@Fluent
@Beta
public interface SpringApps
    extends HasManager<AppPlatformManager>,
        HasInner<AppsClient>,
        HasParent<SpringService>,
        SupportsCreating<SpringApp.DefinitionStages.Blank>,
        SupportsGettingById<SpringApp>,
        SupportsGettingByName<SpringApp>,
        SupportsListing<SpringApp>,
        SupportsDeletingById,
        SupportsDeletingByName {
}
