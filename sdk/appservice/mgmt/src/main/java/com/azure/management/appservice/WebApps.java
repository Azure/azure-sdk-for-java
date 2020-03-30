/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.annotation.Fluent;
import com.azure.management.appservice.implementation.AppServiceManager;
import com.azure.management.appservice.models.WebAppsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for web app management API.
 */
@Fluent
public interface WebApps extends
        SupportsCreating<WebApp.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListing<WebApp>,
        SupportsListingByResourceGroup<WebApp>,
        SupportsGettingByResourceGroup<WebApp>,
        SupportsGettingById<WebApp>,
        SupportsDeletingByResourceGroup,
        HasManager<AppServiceManager>,
        HasInner<WebAppsInner> {
}
