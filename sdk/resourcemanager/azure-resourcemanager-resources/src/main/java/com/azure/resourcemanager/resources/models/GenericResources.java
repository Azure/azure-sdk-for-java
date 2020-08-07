// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingInResourceGroupByTag;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Entry point to generic resources management API.
 */
@Fluent
public interface GenericResources extends
        SupportsListing<GenericResource>,
        SupportsListingByResourceGroup<GenericResource>,
        SupportsListingInResourceGroupByTag<GenericResource>,
        SupportsGettingById<GenericResource>,
        SupportsCreating<GenericResource.DefinitionStages.Blank>,
        SupportsDeletingById,
        HasManager<ResourceManager> {
    /**
     * Checks if a resource exists in a resource group.
     *
     * @param resourceGroupName the resource group's name
     * @param resourceProviderNamespace the resource provider's namespace
     * @param parentResourcePath the parent's resource path
     * @param resourceType the type of the resource
     * @param resourceName the name of the resource
     * @param apiVersion the API version
     * @return true if the resource exists; false otherwise
     */
    boolean checkExistence(
            String resourceGroupName,
            String resourceProviderNamespace,
            String parentResourcePath,
            String resourceType,
            String resourceName,
            String apiVersion);

    /**
     * Checks if a resource exists.
     *
     * @param id the ID of the resource.
     * @return true if the resource exists; false otherwise
     */
    boolean checkExistenceById(String id);

    /**
     * Returns a resource belonging to a resource group.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @param apiVersion the String value
     * @return the generic resource
     */
    GenericResource get(
            String resourceGroupName,
            String resourceProviderNamespace,
            String parentResourcePath,
            String resourceType,
            String resourceName,
            String apiVersion);

    /**
     * Returns a resource belonging to a resource group.
     *
     * @param resourceGroupName the resource group name
     * @param providerNamespace the provider namespace
     * @param resourceType the resource type
     * @param resourceName the name of the resource
     * @return the generic resource
     */
    GenericResource get(
            String resourceGroupName,
            String providerNamespace,
            String resourceType,
            String resourceName);

    /**
     * Move resources from one resource group to another.
     *
     * @param sourceResourceGroupName Source resource group name
     * @param targetResourceGroup target resource group, can be in a different subscription
     * @param resources the list of IDs of the resources to move
     */
    void moveResources(String sourceResourceGroupName, ResourceGroup targetResourceGroup, List<String> resources);

    /**
     * Move resources from one resource group to another asynchronously.
     *
     * @param sourceResourceGroupName Source resource group name
     * @param targetResourceGroup target resource group, can be in a different subscription
     * @param resources the list of IDs of the resources to move
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> moveResourcesAsync(String sourceResourceGroupName,
                                  ResourceGroup targetResourceGroup, List<String> resources);

    /**
     * Delete resource and all of its child resources.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @param apiVersion the String value
     */
    void delete(String resourceGroupName, String resourceProviderNamespace,
                String parentResourcePath, String resourceType, String resourceName, String apiVersion);


    /**
     * Delete resource and all of its child resources asynchronously.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @param apiVersion the String value
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync(String resourceGroupName, String resourceProviderNamespace,
                           String parentResourcePath, String resourceType, String resourceName, String apiVersion);

    /**
     * Begins deleting a resource from Azure, identifying it by its resource ID.
     *
     * @param id the resource ID of the resource to delete
     * @return the accepted deleting operation
     */
    Accepted<Void> beginDeleteById(String id);
}
