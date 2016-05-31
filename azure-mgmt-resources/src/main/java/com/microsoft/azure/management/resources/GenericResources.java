/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

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
     * Entry point to generic resources management API in a specific resource group.
     */
    interface InGroup extends
            SupportsListing<GenericResource>,
            SupportsGettingByName<GenericResource> {
    }
}
