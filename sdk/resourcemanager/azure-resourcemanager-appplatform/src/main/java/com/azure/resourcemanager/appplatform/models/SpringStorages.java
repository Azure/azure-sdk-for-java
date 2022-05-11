// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/** Entry point for Spring Storage API. */
@Fluent
public interface SpringStorages<T>
    extends HasManager<AppPlatformManager>,
    SupportsCreating<SpringStorage.DefinitionStages.Blank<T>>,
    SupportsGettingByName<SpringStorage>,
    SupportsListing<SpringStorage> {

}
