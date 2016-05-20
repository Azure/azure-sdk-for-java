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
 * Defines the base interface for resources that support listing resources
 * in a resource group.
 *
 * @param <T> the type of the resources listed.
 */
public interface SupportsListingByGroup<T> {
    /**
     * List resources in the resource group.
     *
     * @param groupName the name of the resource group.
     * @return the list of resources.
     * @throws CloudException exception thrown from the cloud.
     * @throws IOException exception thrown from serialization/deserialization.
     */
    PagedList<T> list(String groupName) throws CloudException, IOException;
}
