// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/** Entry point for web app management API. */
@Fluent
public interface WebApps
    extends SupportsCreating<WebApp.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListing<WebAppBasic>,
        SupportsListingByResourceGroup<WebAppBasic>,
        SupportsGettingByResourceGroup<WebApp>,
        SupportsGettingById<WebApp>,
        SupportsDeletingByResourceGroup,
        HasManager<AppServiceManager> {
}
