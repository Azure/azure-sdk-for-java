/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListingAsync;

/**
 * Entry point for storage accounts management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public interface DeploymentSlots extends
        SupportsCreating<DeploymentSlot.DefinitionStages.Blank>,
        SupportsListingAsync<DeploymentSlot>,
        SupportsGettingByName<DeploymentSlot>,
        SupportsDeletingById,
        SupportsGettingById<DeploymentSlot>,
        SupportsDeletingByName,
        HasManager<AppServiceManager>,
        HasParent<WebApp> {
}