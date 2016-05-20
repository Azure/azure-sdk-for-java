/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection;

import com.microsoft.azure.CloudException;

import java.io.IOException;

// Requires class to support reading entities with a supplied group name

/**
 * Defines the base interface for resources that support getting a resource
 * from the resource group it belongs to.
 *
 * @param <T> the type of the resource to get.
 */
public interface SupportsGettingByGroup<T> {
    /**
     * Get the resource from a resource group.
     *
     * @param groupName the name of the resource group.
     * @param name the name of the resource.
     * @return the resource got from the cloud.
     * @throws CloudException exceptions thrown from the cloud.
     * @throws IOException exceptions thrown from serialization/deserialization.
     */
    T get(String groupName, String name) throws CloudException, IOException;
}
