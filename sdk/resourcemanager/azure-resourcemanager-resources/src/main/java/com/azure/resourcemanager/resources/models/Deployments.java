// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.ResourceManager;

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
