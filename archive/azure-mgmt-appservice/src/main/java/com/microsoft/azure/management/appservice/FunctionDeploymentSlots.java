/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point for Azure function app deployment slot management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
@Beta(Beta.SinceVersion.V1_3_0)
public interface FunctionDeploymentSlots extends
        SupportsCreating<FunctionDeploymentSlot.DefinitionStages.Blank>,
        SupportsListing<FunctionDeploymentSlot>,
        SupportsGettingByName<FunctionDeploymentSlot>,
        SupportsGettingById<FunctionDeploymentSlot>,
        SupportsDeletingById,
        SupportsDeletingByName,
        HasManager<AppServiceManager>,
        HasParent<FunctionApp> {
}