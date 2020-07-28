// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.WebAppsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point for web app management API. */
@Fluent
public interface FunctionApps
    extends SupportsCreating<FunctionApp.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListing<FunctionApp>,
        SupportsListingByResourceGroup<FunctionApp>,
        SupportsGettingByResourceGroup<FunctionApp>,
        SupportsGettingById<FunctionApp>,
        SupportsDeletingByResourceGroup,
        HasManager<AppServiceManager>,
        HasInner<WebAppsClient> {

    /**
     * List function information elements.
     *
     * @param resourceGroupName resource group name
     * @param name function app name
     * @return list of function information elements
     */
    PagedIterable<FunctionEnvelope> listFunctions(String resourceGroupName, String name);
}
