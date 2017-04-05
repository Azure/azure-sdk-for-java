/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.implementation.ResourceManager;

/**
 * Entry point to template deployment in Azure.
 */
@Fluent
public interface Deployments extends
        SupportsCreating<Deployment.DefinitionStages.Blank>,
        SupportsListing<Deployment>,
        SupportsListingByResourceGroup<Deployment>,
        SupportsGettingByName<Deployment>,
        SupportsGettingByResourceGroup<Deployment>,
        SupportsGettingById<Deployment>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        HasManager<ResourceManager> {
    /**
     * Checks if a deployment exists in a resource group.
     *
     * @param resourceGroupName the resource group's name
     * @param deploymentName the deployment's name
     * @return true if the deployment exists; false otherwise
     */
    boolean checkExistence(String resourceGroupName, String deploymentName);
}
