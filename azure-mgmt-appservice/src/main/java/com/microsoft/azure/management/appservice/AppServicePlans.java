/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import rx.Observable;

/**
 * Entry point for app service plan management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public interface AppServicePlans extends
        SupportsCreating<AppServicePlan.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByGroup<AppServicePlan>,
        SupportsGettingByGroup<AppServicePlan>,
        SupportsGettingById<AppServicePlan>,
        SupportsDeletingByGroup {
    /**
     * Gets the information about a resource from Azure based on the resource name and the name of its resource group.
     *
     * @param id the app service plan resource ID
     * @return an immutable representation of the resource
     */
    Observable<AppServicePlan> getByIdAsync(String id);
}