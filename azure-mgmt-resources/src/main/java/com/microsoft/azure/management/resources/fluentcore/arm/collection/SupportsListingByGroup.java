/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;

import java.io.IOException;

/**
 * Provides access to listing Azure resources of a specific type in a specific resource group.
 *
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the type of the resources listed.
 */
public interface SupportsListingByGroup<T> {
    /**
     * Lists resources of the specified type in the specified resource group.
     *
     * @param resourceGroupName the name of the resource group to list the resources from
     * @return the list of resources
     * @throws CloudException exception thrown from the cloud
     * @throws IOException exception thrown from serialization/deserialization
     */
    PagedList<T> listByGroup(String resourceGroupName) throws CloudException, IOException;
}
