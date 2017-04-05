/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.azure.management.appservice.implementation.WebAppsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for web app management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
@Beta
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
