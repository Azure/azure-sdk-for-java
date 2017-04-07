/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.azure.management.appservice.implementation.AppServicePlansInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point for App Service plan management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
@Beta
public interface AppServicePlans extends
        SupportsCreating<AppServicePlan.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByResourceGroup<AppServicePlan>,
        SupportsListing<AppServicePlan>,
        SupportsGettingByResourceGroup<AppServicePlan>,
        SupportsGettingById<AppServicePlan>,
        SupportsDeletingByResourceGroup,
        HasManager<AppServiceManager>,
        HasInner<AppServicePlansInner> {
}
