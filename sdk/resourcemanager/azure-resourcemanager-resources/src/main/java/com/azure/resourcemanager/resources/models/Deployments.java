// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;

/**
 * Entry point to template deployment in Azure.
 */
@Fluent
public interface Deployments extends SupportsCreating<Deployment.DefinitionStages.Blank>, SupportsListing<Deployment>,
    SupportsListingByResourceGroup<Deployment>, SupportsGettingByName<Deployment>,
    SupportsGettingByResourceGroup<Deployment>, SupportsGettingById<Deployment>, SupportsDeletingById,
    SupportsDeletingByResourceGroup, HasManager<ResourceManager> {
    /**
     * Checks if a deployment exists in a resource group.
     *
     * @param resourceGroupName the resource group's name
     * @param deploymentName the deployment's name
     * @return true if the deployment exists; false otherwise
     */
    boolean checkExistence(String resourceGroupName, String deploymentName);

    /**
     * Deletes a deployment from the deployment history by its ID.
     * A template deployment that is currently running cannot be deleted. Deleting a template deployment removes the
     * associated deployment operations. Deleting a template deployment does not affect the state of the resource group.
     *
     * @param id the resource ID of the resource to delete
     * @return the accepted deleting operation
     */
    default Accepted<Void> beginDeleteById(String id) {
        throw new UnsupportedOperationException("[beginDeleteById(String)] is not supported in " + getClass());
    }

    /**
     * Deletes a deployment from the deployment history by its ID.
     * A template deployment that is currently running cannot be deleted. Deleting a template deployment removes the
     * associated deployment operations. Deleting a template deployment does not affect the state of the resource group.
     *
     * @param id the resource ID of the resource to delete
     * @param context the {@link Context} of the request
     * @return the accepted deleting operation
     */
    default Accepted<Void> beginDeleteById(String id, Context context) {
        throw new UnsupportedOperationException("[beginDeleteById(String, Context)] is not supported in " + getClass());
    }

    /**
     * Deletes a deployment from the deployment history by its resource group and name.
     * A template deployment that is currently running cannot be deleted. Deleting a template deployment removes the
     * associated deployment operations. Deleting a template deployment does not affect the state of the resource group.
     *
     * @param resourceGroupName the resource group the deployment is part of
     * @param name the name of the deployment
     * @return the accepted deleting operation
     */
    default Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name) {
        throw new UnsupportedOperationException(
            "[beginDeleteByResourceGroup(String, String)] is not supported in " + getClass());
    }

    /**
     * Deletes a deployment from the deployment history by its resource group and name.
     * A template deployment that is currently running cannot be deleted. Deleting a template deployment removes the
     * associated deployment operations. Deleting a template deployment does not affect the state of the resource group.
     *
     * @param resourceGroupName the resource group the deployment is part of
     * @param name the name of the deployment
     * @param context the {@link Context} of the request
     * @return the accepted deleting operation
     */
    default Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name, Context context) {
        throw new UnsupportedOperationException(
            "[beginDeleteByResourceGroup(String, String, Context)] is not supported in " + getClass());
    }
}
