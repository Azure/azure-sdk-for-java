/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;
import java.util.List;

/**
 * Entry point to generic resources management API.
 */
public interface GenericResources extends
        SupportsListing<GenericResource>,
        SupportsListingByGroup<GenericResource>,
        SupportsGettingByGroup<GenericResource>,
        SupportsCreating<GenericResource.DefinitionBlank>,
        SupportsDeleting,
        SupportsDeletingByGroup {
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
     * @throws IOException serialization failures
     * @throws CloudException failures thrown from Azure
     */
    boolean checkExistence(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) throws IOException, CloudException;

    /**
     * Move resources from one resource group to another.
     *
     * @param sourceResourceGroupName Source resource group name
     * @param targetResourceGroup target resource group, can be in a different subscription
     * @param resources the list of IDs of the resources to move
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws InterruptedException exception thrown when long running operation is interrupted
     */
    void moveResources(String sourceResourceGroupName, ResourceGroup targetResourceGroup, List<String> resources) throws CloudException, IOException, InterruptedException;
}
