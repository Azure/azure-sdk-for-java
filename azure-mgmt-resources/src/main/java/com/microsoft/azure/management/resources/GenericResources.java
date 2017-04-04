/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingInResourceGroupByTag;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;

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
     *
     * @return a representation of the deferred computation of this call
     */
    Completable moveResourcesAsync(String sourceResourceGroupName, ResourceGroup targetResourceGroup, List<String> resources);

    /**
     * Move resources from one resource group to another asynchronously.
     *
     * @param sourceResourceGroupName Source resource group name
     * @param targetResourceGroup target resource group, can be in a different subscription
     * @param resources the list of IDs of the resources to move
     * @param callback the callback to call on success or failure
     *
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> moveResourcesAsync(String sourceResourceGroupName, ResourceGroup targetResourceGroup, List<String> resources, ServiceCallback<Void> callback);

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
    void delete(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion);


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
    Completable deleteAsync(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion);


    /**
     * Delete resource and all of its child resources asynchronously.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param resourceProviderNamespace Resource identity.
     * @param parentResourcePath Resource identity.
     * @param resourceType Resource identity.
     * @param resourceName Resource identity.
     * @param apiVersion the String value
     * @param callback the callback to call on success or failure
     *
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> deleteAsync(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion, ServiceCallback<Void> callback);
}
